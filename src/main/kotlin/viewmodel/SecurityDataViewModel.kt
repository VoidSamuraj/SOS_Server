package viewmodel

import dao.DaoMethods
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.dto.GuardInfo
import models.entity.Guard
import models.entity.Intervention
import models.entity.Report
import plugins.interventionResponseChannel

object SecurityDataViewModel {

    //Stored in System
    private val _guardsFlow = MutableStateFlow<List<GuardInfo>>(emptyList())
    val guardsFlow: Flow<List<GuardInfo>> get() = _guardsFlow.asStateFlow()

    private val _reportsFlow = MutableStateFlow<List<Report>>(emptyList())
    val reportsFlow: Flow<List<Report>> get() = _reportsFlow.asStateFlow()

    val clientSessions = mutableMapOf<Int, DefaultWebSocketSession>()
    val guardSessions = mutableMapOf<Int, DefaultWebSocketSession>()

    suspend fun addReport(report: Report, clientSession: DefaultWebSocketSession): Int {
        clientSessions.put(report.client_id, clientSession)
        val id = DaoMethods.addReport(report.client_id, report.location, report.date, report.status)
        if (id != -1) {
            _reportsFlow.value = _reportsFlow.value + report.copy(id = id)
        }
        return id
    }

    suspend fun addClientSession(clientId: Int, clientSession: DefaultWebSocketSession) {
        clientSessions.put(clientId, clientSession)
    }

    suspend fun finishReport(reportId: Int): Boolean {
        val report = DaoMethods.changeReportStatus(reportId, Report.ReportStatus.FINISHED)
        editReportStatus(reportId, Report.ReportStatus.FINISHED)
        if (report != null) {
            clientSessions[report.client_id]?.send(Frame.Text("""{"status": finished}"""))
            CoroutineScope(Dispatchers.IO).launch {
                delay(10_000)
                _reportsFlow.value = _reportsFlow.value.filter { report -> reportId != report.id }
            }
        }
        return report != null
    }

    suspend fun editReportStatus(id: Int, status: Report.ReportStatus, saveInDB: Boolean=true) {
        if(saveInDB)
            DaoMethods.changeReportStatus(id, status)
        _reportsFlow.value = _reportsFlow.value.map { reportRow ->
            if (reportRow.id == id) {
                reportRow.copy(statusCode = status.status.toShort())
            } else {
                reportRow
            }
        }
    }

    suspend fun editReportLocation(id: Int, location: String) {
        DaoMethods.getActiveInterventionByReport(id)?.let{intervention->
            guardSessions[intervention.guard_id]?.send(Frame.Text("""{"status":update, "reportId": ${intervention.report_id},"location": ${location}}"""))
        }

        _reportsFlow.value = _reportsFlow.value.map { reportRow ->
            if (reportRow.id == id) {
                reportRow.copy(location = location)
            } else {
                reportRow
            }
        }
    }

    fun addGuard(guard: GuardInfo, guardSession: DefaultWebSocketSession) {
        if (guard.id != -1) {
            guardSessions.put(guard.id, guardSession)
            val currentGuards = _guardsFlow.value
            _guardsFlow.value = currentGuards + guard
        }
    }

    fun editGuard(id: Int, location: String, status: Int) {
        _guardsFlow.value = _guardsFlow.value.map { guardRow ->
            if (guardRow.id == id) {
                guardRow.copy(location = location, statusCode = status)
            } else {
                guardRow
            }
        }
    }

    fun editGuardStatus(guardSession: DefaultWebSocketSession, status: Guard.GuardStatus) {
        val guardKey = guardSessions.entries.firstOrNull { it.value == guardSession }?.key
        if (guardKey != null) {
            editGuardStatus(guardKey, status)
        }
    }

    fun editGuardStatus(id: Int, status: Guard.GuardStatus) {
        _guardsFlow.value = _guardsFlow.value.map { guardRow ->
            if (guardRow.id == id) {
                guardRow.copy(statusCode = status.status)
            } else {
                guardRow
            }
        }
    }

    suspend fun getAssignedGuardIdByReportId(reportId:Int):Int?{
        return DaoMethods.getActiveInterventionByReport(reportId)?.guard_id
    }

    fun setReports(reports: List<Report>) {
        _reportsFlow.value = reports
    }

    fun setGuards(guards: List<GuardInfo>) {
        _guardsFlow.value = guards
    }

    fun getGuardLocationById(guardId: Int): String? {
        return _guardsFlow.value.find { it.id == guardId }?.location
    }

    fun getReportsLocationById(reportId: Int): String? {
        return _reportsFlow.value.find { it.id == reportId }?.location
    }

    suspend fun assignReportToGuard(
        reportId: Int,
        guardId: Int,
        onConfirm: suspend () -> Unit,
        onCancel: suspend () -> Unit,
        onReportCancel: suspend () -> Unit,
        onFailure: suspend () -> Unit
    ) {
        val report = DaoMethods.getReport(reportId)
        if (report != null) {
            guardSessions[guardId]?.send(Frame.Text("""{"status":confirm, "reportId": ${report.id},"location": ${report.location}}"""))
            if (guardSessions[guardId] == null) {
                onFailure()
            }
            try {
                withTimeout(30_000) {
                    val response = interventionResponseChannel.receive()
                    if (response == "cancel")
                        onCancel()
                    else if (response == "accept") {
                        editReportStatus(id = reportId, status = Report.ReportStatus.IN_PROGRESS)
                        clientSessions[report.client_id]?.send(Frame.Text("""{"status": confirmed}"""))
                        onConfirm()
                    }
                }
            } catch (_: TimeoutCancellationException) {
                val report = DaoMethods.getReport(reportId)
                if (report != null && report.status != Report.ReportStatus.FINISHED)
                    onFailure()
                else
                    onReportCancel()
            }

        }
    }

    suspend fun addIntervention(reportId: Int, guardId: Int, employeeId: Int): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return DaoMethods.addIntervention(
            reportId = reportId,
            guardId = guardId,
            employeeId = employeeId,
            startTime = now,
            endTime = now,
            status = Intervention.InterventionStatus.CONFIRMED
        )
    }

    suspend fun editIntervention(
        reportId: Int,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
        status: Intervention.InterventionStatus? = null
    ): Boolean {
        return DaoMethods.editIntervention(reportId, startTime, endTime, status, true)
    }

    //sends message if there is intervention
    suspend fun finishInterventionByUser(
        reportId: Int,
    ): Boolean {
        val intervention = DaoMethods.getActiveInterventionByReport(reportId,filterActive = false)
        if (DaoMethods.editIntervention(
                reportId,
                null,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                Intervention.InterventionStatus.CANCELLED_BY_USER,
                true
            )
        ) {
            intervention?.let { intervention ->
                guardSessions[intervention.guard_id]?.send(Frame.Text("""{"status": cancel}"""))
                return (guardSessions[intervention.guard_id] != null)
            }
            return false
        } else {
            return false
        }
    }

    suspend fun finishInterventionByDispatcher(
        reportId: Int,
    ): Boolean {
        val intervention = DaoMethods.getActiveInterventionByReport(reportId, filterActive = false)
        if (
            DaoMethods.editIntervention(
                reportId,
                null,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                Intervention.InterventionStatus.CANCELLED_BY_DISPATCHER,
                true
            )
        ) {
            intervention?.let { intervention ->
                guardSessions[intervention.guard_id]?.send(Frame.Text("""{"status": cancel}"""))
                return (guardSessions[intervention.guard_id] != null)
            }
            return false
        } else {
            return false
        }

    }

    suspend fun callSupport(reportId: Int): Boolean {
        val ret = DaoMethods.changeReportStatus(reportId, Report.ReportStatus.WAITING)
        if(ret!=null){
            editReportStatus(reportId, Report.ReportStatus.WAITING)
        }
        return ret != null
    }

    suspend fun sendWarningToGuard(
        guardId: Int
    ): Boolean {
        guardSessions[guardId]?.send(Frame.Text("""{"status": warning}"""))
        return (guardSessions[guardId] != null)
    }
}