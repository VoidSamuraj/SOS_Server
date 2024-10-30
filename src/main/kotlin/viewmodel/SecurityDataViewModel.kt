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
import kotlinx.coroutines.flow.map
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

    private val _reportsFlow = MutableStateFlow<MutableMap<Int, Report>>(mutableMapOf())
    val reportsFlow: Flow<List<Report>> get() = _reportsFlow.map { it.values.toList() }

    val clientSessions = mutableMapOf<Int, DefaultWebSocketSession>()
    val guardSessions = mutableMapOf<Int, DefaultWebSocketSession>()

    suspend fun addReport(report: Report): Int {
        val id = DaoMethods.addReport(report.client_id, report.location, report.date, report.status)
        if (id != -1)
            _reportsFlow.value.put(report.id, report)
        return id
    }

    suspend fun finishReport(reportId: Int): Boolean {
        val success = DaoMethods.changeReportStatus(reportId, Report.ReportStatus.FINISHED)
        editReportStatus(reportId, Report.ReportStatus.FINISHED)
        if (success) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(10_000)
                _reportsFlow.value.remove(reportId)
            }
        }
        return success
    }

    suspend fun editReportStatus(id: Int, status: Report.ReportStatus) {
        _reportsFlow.value = _reportsFlow.value.mapValues { (key, report) ->
            if (key == id) {
                report.copy(statusCode = status.status.toShort())
            } else {
                report
            }
        }.toMutableMap()
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

    fun editGuardStatus(id: Int, status: Guard.GuardStatus) {
        _guardsFlow.value = _guardsFlow.value.map { guardRow ->
            if (guardRow.id == id) {
                guardRow.copy(statusCode = status.status)
            } else {
                guardRow
            }
        }
    }

    fun setReports(reports: Map<Int, Report>) {
        _reportsFlow.value = reports.toMutableMap()
    }

    fun setGuards(guards: List<GuardInfo>) {
        _guardsFlow.value = guards
    }

    suspend fun assignReportToGuard(
        reportId: Int,
        guardId: Int,
        onConfirm: suspend () -> Unit,
        onCancel: suspend () -> Unit,
        onFailure: suspend () -> Unit
    ) {
        val report = DaoMethods.getReport(reportId)
        if (report != null) {
            guardSessions[guardId]?.send(Frame.Text("""{"status":confirm, "reportId": ${report.id},"location": ${report.location}}"""))
            if (guardSessions[guardId] == null)
                onFailure()
            try {
                withTimeout(30_000) {
                    val response = interventionResponseChannel.receive()
                    if (response == "cancel")
                        onCancel()
                    else if (response == "accept") {
                        onConfirm()
                    }
                }
            } catch (_: TimeoutCancellationException) {
                onFailure()
            }

        } else
            onFailure()
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
        return DaoMethods.editIntervention(reportId, startTime, endTime, status)
    }

    suspend fun callSupport(reportId: Int): Boolean {
        val ret = DaoMethods.changeReportStatus(reportId, Report.ReportStatus.WAITING)
        return ret
    }

    suspend fun sendWarningToGuard(
        guardId: Int
    ): Boolean {
        guardSessions[guardId]?.send(Frame.Text("""{"status": warning}"""))
        return (guardSessions[guardId] != null)
    }
}