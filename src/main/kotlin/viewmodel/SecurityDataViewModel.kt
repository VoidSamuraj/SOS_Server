package viewmodel

import dao.DaoMethods
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SecurityDataViewModel {
    private val _guardsFlow = MutableStateFlow<List<GuardInfo>>(emptyList())
    val guardsFlow: Flow<List<GuardInfo>> get() = _guardsFlow.asStateFlow()

    private val _reportsFlow = MutableStateFlow<List<Report>>(emptyList())
    val reportsFlow: MutableStateFlow<List<Report>> get() = _reportsFlow

    suspend fun addReport(report: Report): Int {
       // val id = DaoMethods.addReport(report.client_id, report.location, report.date, report.status)
        val currentReports = _reportsFlow.value
        _reportsFlow.value = currentReports + report
        return 1//id
    }

    suspend fun editReportStatus(id: Int, status: Report.ReportStatus): Boolean {
        val success = DaoMethods.changeReportStatus(id, status)
        val currentReports = _reportsFlow.value
      //  _reportsFlow.value = currentReports + report
        return true //success
    }
    fun addGuard(guard: GuardInfo) {
        val currentGuards = _guardsFlow.value
        _guardsFlow.value = currentGuards + guard
    }

    fun setReports(reports: List<Report>) {
        _reportsFlow.value = reports
    }

    fun setGuards(guards: List<GuardInfo>) {
        _guardsFlow.value = guards
    }
}