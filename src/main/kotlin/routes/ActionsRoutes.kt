package routes

import dao.DaoMethods
import dao.DaoMethods.getActiveInterventionByReport
import dao.DaoMethods.getInterventionByGuard
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.entity.Guard
import models.entity.Report
import plugins.sanitizeHtml
import security.JWTToken
import viewmodel.SecurityDataViewModel

fun Route.actionRoutes() {
    route("/action") {
        intercept(ApplicationCallPipeline.Plugins) {
            try {
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    checkUserPermission(onSuccess = {
                        proceed()
                    }, onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "You do not have permission to perform this action.")
                        finish()
                    })
                } else {
                    val token = authHeader.removePrefix("Bearer ")
                    checkUserPermission(
                        JWTToken(token),
                        onSuccess = {
                            proceed()
                        },
                        onFailure = {
                            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                            finish()
                        }
                    )
                }
            } catch (e: Exception) {
                println("Wystąpił błąd w interceptorze: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                finish()
            }
        }

        route("/assignGuardToReport") {
            post {
                val formParameters = call.receiveParameters()
                val reportId = sanitizeHtml(formParameters.getOrFail("reportId")).toInt()
                val guardId = sanitizeHtml(formParameters.getOrFail("guardId")).toInt()
                val employeeId = sanitizeHtml(formParameters.getOrFail("employeeId")).toInt()
                launch(Dispatchers.Default) {
                    SecurityDataViewModel.editReportStatus(id = reportId, status = Report.ReportStatus.IN_PROGRESS, saveInDB = false)
                    SecurityDataViewModel.assignReportToGuard(
                        reportId = reportId, guardId = guardId,
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
                        }, onReportCancel = {
                            SecurityDataViewModel.editGuardStatus(
                                id = guardId,
                                status = Guard.GuardStatus.UNAVAILABLE
                            )
                        },
                        onFailure = {
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

        post("/cancelIntervention") {
            val formParameters = call.receiveParameters()
            val reportId = sanitizeHtml(formParameters.getOrFail("reportId")).toInt()
            val intervention = getActiveInterventionByReport(reportId = reportId)
            SecurityDataViewModel.finishInterventionByDispatcher(reportId = reportId)
            SecurityDataViewModel.editReportStatus(id = reportId, status = Report.ReportStatus.WAITING)
            intervention?.let {
                SecurityDataViewModel.editGuardStatus(id = it.guard_id, status = Guard.GuardStatus.AVAILABLE)
            }
            call.respond(HttpStatusCode.OK)
        }

        post("/getAssignedGuardLocation") {
            val formParameters = call.receiveParameters()
            val reportId = sanitizeHtml(formParameters.getOrFail("reportId")).toInt()
            val intervention = getActiveInterventionByReport(reportId = reportId)
            if (intervention != null) {
                val location =
                    SecurityDataViewModel.getGuardLocationById(intervention.guard_id)?.replace("lat", "\"lat\"")
                        ?.replace("lng", "\"lng\"")
                if (location != null)
                    call.respond(HttpStatusCode.OK, location)
                else
                    call.respond(HttpStatusCode.NoContent)
            }
            call.respond(HttpStatusCode.NoContent)
        }

        post("/getAssignedReportLocation") {
            val formParameters = call.receiveParameters()
            val guardId = sanitizeHtml(formParameters.getOrFail("guardId")).toInt()
            val intervention = getInterventionByGuard(guardId = guardId)
            if (intervention != null) {
                val location =
                    SecurityDataViewModel.getReportsLocationById(intervention.report_id)?.replace("lat", "\"lat\"")
                        ?.replace("lng", "\"lng\"")
                if (location != null)
                    call.respond(HttpStatusCode.OK, location)
                else
                    call.respond(HttpStatusCode.NoContent)
            }
            call.respond(HttpStatusCode.NoContent)
        }



        get("/checkConnection") {
            call.respond(HttpStatusCode.OK)
        }

        post("/getActiveInterventionLocationAssignedToGuard") {
            val formParameters = call.receiveParameters()
            val guardId = sanitizeHtml(formParameters.getOrFail("guardId")).toInt()
            val location = DaoMethods.isActiveInterventionAssignedToGuard(guardId)
            if (location != null)
                call.respond(HttpStatusCode.OK, location)
            else
                call.respond(HttpStatusCode.NotFound)
        }

        post("/checkIfReportIsAvailable") {
            val formParameters = call.receiveParameters()
            val reportId = sanitizeHtml(formParameters.getOrFail("reportId")).toInt()
            val report = DaoMethods.getReport(reportId)
            if (report == null) {
                call.respond(HttpStatusCode.NotFound)
            } else
                call.respond(HttpStatusCode.OK, report.status == Report.ReportStatus.WAITING)

        }

    }
}