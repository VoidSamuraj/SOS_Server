package routes

import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.entity.Guard
import models.entity.Report
import plugins.sanitizeHtml
import viewmodel.SecurityDataViewModel

fun Route.actionRoutes() {
    route("/action") {

        route("/assignGuardToReport") {
            intercept(ApplicationCallPipeline.Plugins) {
                checkUserPermission(onSuccess = {
                    proceed()
                }, onFailure = {
                    call.respond(HttpStatusCode.Unauthorized, "You do not have permission to perform this action.")
                    finish()
                })
            }
            post {
                val formParameters = call.receiveParameters()
                val reportId = sanitizeHtml(formParameters.getOrFail("reportId")).toInt()
                val guardId = sanitizeHtml(formParameters.getOrFail("guardId")).toInt()
                val employeeId = sanitizeHtml(formParameters.getOrFail("employeeId")).toInt()
                launch(Dispatchers.Default) {
                    SecurityDataViewModel.editReportStatus(id = reportId, status = Report.ReportStatus.IN_PROGRESS)
                    SecurityDataViewModel.assignReportToGuard(reportId = reportId, guardId = guardId,
                        onConfirm = {
                            SecurityDataViewModel.editReportStatus(
                                id = reportId,
                                status = Report.ReportStatus.IN_PROGRESS
                            )
                            SecurityDataViewModel.editGuardStatus(id = guardId, status = Guard.GuardStatus.INTERVENTION)
                            SecurityDataViewModel.addIntervention(
                                reportId = reportId,
                                guardId = guardId,
                                employeeId = employeeId
                            )
                        }, onCancel = {
                            SecurityDataViewModel.editReportStatus(id = reportId, status = Report.ReportStatus.WAITING)
                            SecurityDataViewModel.editGuardStatus(id = guardId, status = Guard.GuardStatus.AVAILABLE)
                        }, onFailure = {
                            SecurityDataViewModel.editReportStatus(reportId, status = Report.ReportStatus.WAITING)
                            SecurityDataViewModel.sendWarningToGuard(guardId = guardId)
                            SecurityDataViewModel.editGuardStatus(
                                id = guardId,
                                status = Guard.GuardStatus.NOT_RESPONDING
                            )
                        })
                }
                call.respond(HttpStatusCode.OK, "Success")
            }
        }
        post("/getActiveInterventionLocationAssignedToGuard") {
            val formParameters = call.receiveParameters()
            val guardId = sanitizeHtml(formParameters.getOrFail("guardId")).toInt()
            val location = DaoMethods.isActiveInterventionAssignedToGuard(guardId)
            println(location)
            if (location != null)
                call.respond(HttpStatusCode.OK, location)
            else
                call.respond(HttpStatusCode.NotFound)
        }
    }
}