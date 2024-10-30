package plugins

import administrationQueryParams
import administrationSelectedRowsIds
import com.google.gson.JsonParser
import dao.DaoMethods
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.sessions
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import models.dto.CustomerInfo
import models.dto.EmployeeInfo
import models.dto.GuardInfo
import models.entity.Guard
import models.entity.Intervention
import models.entity.Report
import routes.CustomerField
import routes.EmployeeField
import routes.GuardField
import security.JWTToken
import security.checkPermission
import viewmodel.SecurityDataViewModel


@Serializable
data class QueryParams(
    val table: String,
    val page: Int? = 0,
    val pageSize: Int? = 25,
    val filterColumnName: String? = null,
    val filterOperator: String? = null,
    val filterValue: String? = null,
    val sortColumnName: String? = null,
    val sortOrder: String? = null,

    )

@Serializable
data class ResponseWithColumn<T>(
    val columnName: String?,
    val data: List<T>
)

//channel to get redirected responses from websocket
val interventionResponseChannel = Channel<String>(Channel.BUFFERED)

fun Application.configureSockets() {
    install(WebSockets) {
        maxFrameSize = 1024 * 1024
        masking = false
    }
    routing {

        webSocket("/adminPanelSocket") {
            val token = call.sessions.get("userToken") as JWTToken?
            checkPermission(token = token,
                onSuccess = {
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val newParams = Json.decodeFromString<QueryParams>(frame.readText())
                                val previousParams = administrationQueryParams[this]

                                if (previousParams == null || previousParams != newParams) {
                                    loadDataAccordingToParams(this, newParams)
                                    administrationQueryParams[this] = newParams
                                }
                            }

                            if (frame is Frame.Close) {
                                administrationQueryParams.remove(this)
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        println(e)
                        administrationQueryParams.remove(this)
                    }
                },
                onFailure = {
                    outgoing.send(Frame.Text("Unauthorized access"))
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                })
        }
        webSocket("/mapSocket") {
            val token = call.sessions.get("userToken") as JWTToken?
            checkPermission(token = token,
                onSuccess = {
                    try {
                        combine(
                            SecurityDataViewModel.guardsFlow,
                            SecurityDataViewModel.reportsFlow
                        ) { employees, customers ->
                            Pair(employees, customers)
                        }.collect { (updatedGuards, updatedReport) ->

                            Json.encodeToString(
                                ListSerializer(GuardInfo.serializer()),
                                updatedGuards
                            )
                            val responseJson = Json.encodeToString(
                                MapSerializer(String.serializer(), JsonElement.serializer()),
                                mapOf(
                                    "updatedGuards" to Json.encodeToJsonElement(
                                        ListSerializer(GuardInfo.serializer()),
                                        updatedGuards
                                    ),
                                    "updatedReports" to Json.encodeToJsonElement(
                                        ListSerializer(Report.serializer()),
                                        updatedReport
                                    )
                                )
                            )
                            outgoing.send(Frame.Text(responseJson))
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        println(e)
                        administrationQueryParams.remove(this)
                    }
                },
                onFailure = {
                    outgoing.send(Frame.Text("Unauthorized access"))
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                })
        }
        webSocket("/clientSocket") {
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            val jsonObject = JsonParser.parseString(receivedText).asJsonObject
                            if (jsonObject.has("latitude") && jsonObject.has("longitude") && jsonObject.has("userId")) {
                                //on location update
                                if (jsonObject.has("reportId") && jsonObject.get("reportId").asInt != -1) {
                                    if (jsonObject.has("status") && jsonObject.get("status").asString == "cancel") {
                                        println("anulowanieeeeas")
                                    }
                                    println(receivedText)
                                    //TODO
                                    //add updating position in system

                                    //first message
                                } else if (jsonObject.has("callReport") && jsonObject.get("callReport").asBoolean == true) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val reportId = SecurityDataViewModel.addReport(
                                            Report(
                                                id = 0,
                                                client_id = jsonObject.get("userId").asInt,
                                                location = """{
                                                    lat: ${jsonObject.get("latitude")}, lng: ${
                                                    jsonObject.get(
                                                        "longitude"
                                                    )
                                                }}""",
                                                date = Clock.System.now()
                                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                                statusCode = Report.ReportStatus.WAITING.status.toShort()
                                            )
                                        )
                                        outgoing.send(
                                            //TODO check in client if id is -1
                                            Frame.Text("""{"reportId": $reportId}""")
                                        )
                                    }
                                }
                            }
                        }

                        else ->
                            println("clientSocket Received Different Type: $frame")
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val closeReason = closeReason.await()
                val closeCode = closeReason?.code
                val closeMessage = closeReason?.message
                //if closing has status 4000 and message contain id of report
                if (closeCode == 4000.toShort() && closeMessage != null) {
                    val jsonObject = JsonParser.parseString(closeMessage).asJsonObject
                    if (jsonObject.has("reportId")) {
                        val reportId = jsonObject.get("reportId").asInt
                        if (reportId != -1) {
                            println("WebSocket zamknięty: $closeMessage")
                            SecurityDataViewModel.finishReport(reportId)
                        }
                    }
                }
            }
        }
        webSocket("/guardSocket") {
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            println(receivedText)
                            val jsonObject = JsonParser.parseString(receivedText).asJsonObject

                            //response after assign
                            if (jsonObject.has("intervention")) {
                                val intervention = jsonObject.get("intervention").asString
                                //redirect response to channel
                                if (intervention == "cancel" || intervention == "accept")
                                    CoroutineScope(Dispatchers.IO).launch {
                                        interventionResponseChannel.send(intervention)
                                    }
                                print("jsonObject")
                                print(jsonObject)
                                if (jsonObject.has("reportId") && jsonObject.get("reportId").asInt != -1) {
                                    val reportId = jsonObject.get("reportId").asInt
                                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    println("STATUSREC")
                                    if (intervention == "confirmArrival") {
                                        println("confirmArrival")
                                        SecurityDataViewModel.editIntervention(
                                            reportId = reportId,
                                            startTime = now,
                                            status = Intervention.InterventionStatus.IN_PROGRESS
                                        )
                                    } else if (intervention == "finish") {
                                        println("finish")
                                        if(SecurityDataViewModel.editIntervention(
                                            reportId = reportId,
                                            endTime = now,
                                            status = Intervention.InterventionStatus.FINISHED
                                        )){
                                            println("ZAKONCZONO INTERWENCJE")
                                        SecurityDataViewModel.finishReport(reportId)
                                        }else
                                            println("NIEZAKONCZONO INTERWENCJE")
                                    } else if (intervention == "cancelStarted") {
                                        println("cancelStarted")
                                        SecurityDataViewModel.editIntervention(
                                            reportId = reportId,
                                            endTime = now,
                                            status = Intervention.InterventionStatus.CANCELLED_BY_GUARD
                                        )
                                        SecurityDataViewModel.editReportStatus(reportId, Report.ReportStatus.WAITING)
                                    } else if (intervention == "supportNeeded") {
                                        println("supportNeeded")
                                        SecurityDataViewModel.callSupport(reportId)
                                    }
                                }

                            } else if (jsonObject.has("guardId") &&
                                jsonObject.has("status") && jsonObject.get("guardId").asInt != -1
                            ) {
                                if (jsonObject.has("latitude") && jsonObject.has("longitude")) {
                                    //init message
                                    if (jsonObject.has("initMessage") && jsonObject.get("initMessage").asBoolean == true) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val guard = DaoMethods.getGuard(jsonObject.get("guardId").asInt)
                                            guard?.location =
                                                """{lat: ${jsonObject.get("latitude")}, lng: ${jsonObject.get("longitude")}}""".trim()
                                            guard?.statusCode = jsonObject.get("status").asInt
                                            if (guard != null) {
                                                SecurityDataViewModel.addGuard(guard.toGuardInfo(), this@webSocket)
                                                outgoing.send(Frame.Text("""{"status": connected}"""))
                                            }
                                        }
                                        //location and status
                                    } else {
                                        SecurityDataViewModel.editGuard(
                                            jsonObject.get("guardId").asInt,
                                            """{lat: ${jsonObject.get("latitude")}, lng: ${jsonObject.get("longitude")}}""".trim(),
                                            jsonObject.get("status").asInt
                                        )
                                    }
                                    //only status
                                } else {
                                    SecurityDataViewModel.editGuardStatus(
                                        jsonObject.get("guardId").asInt,
                                        Guard.GuardStatus.fromInt(jsonObject.get("status").asInt)
                                    )
                                }
                            }
                        }

                        else ->
                            println("clientSocket Received Different Type: $frame")
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun loadDataAccordingToParams(session: DefaultWebSocketSession, queryParams: QueryParams) {
    CoroutineScope(Dispatchers.IO).launch {
        when (queryParams.table) {
            "employees" -> {
                val res = DaoMethods.getEmployees(
                    queryParams.page ?: 0,
                    queryParams.pageSize ?: 0,
                    queryParams.filterColumnName?.let { EmployeeField[it] },
                    queryParams.filterValue,
                    queryParams.filterOperator,
                    queryParams.sortColumnName?.let { EmployeeField[it] },
                    queryParams.sortOrder
                )
                administrationSelectedRowsIds[session] = res.map { it.id }.toTypedArray()
                val response = ResponseWithColumn(
                    "employees",
                    res
                )
                session.send(
                    Frame.Text(
                        Json.encodeToString(
                            ResponseWithColumn.serializer(EmployeeInfo.serializer()),
                            response
                        )
                    )
                )

            }

            "customers" -> {
                var res = DaoMethods.getCustomers(
                    queryParams.page ?: 0,
                    queryParams.pageSize ?: 0,
                    queryParams.filterColumnName?.let { CustomerField[it] },
                    queryParams.filterValue,
                    queryParams.filterOperator,
                    queryParams.sortColumnName?.let { CustomerField[it] },
                    queryParams.sortOrder
                )
                administrationSelectedRowsIds[session] = res.map { it.id }.toTypedArray()
                val response = ResponseWithColumn(
                    "customers",
                    res
                )
                session.send(
                    Frame.Text(
                        Json.encodeToString(
                            ResponseWithColumn.serializer(CustomerInfo.serializer()),
                            response
                        )
                    )
                )
            }

            "guards" -> {
                val res = DaoMethods.getGuards(
                    queryParams.page ?: 0,
                    queryParams.pageSize ?: 0,
                    queryParams.filterColumnName?.let { GuardField[it] },
                    queryParams.filterValue,
                    queryParams.filterOperator,
                    queryParams.sortColumnName?.let { GuardField[it] },
                    queryParams.sortOrder
                )
                administrationSelectedRowsIds[session] = res.map { it.id }.toTypedArray()
                Json.encodeToString(
                    ListSerializer(GuardInfo.serializer()),
                    res
                )
                val response = ResponseWithColumn(
                    "guards",
                    res
                )
                session.send(
                    Frame.Text(
                        Json.encodeToString(
                            ResponseWithColumn.serializer(GuardInfo.serializer()),
                            response
                        )
                    )
                )

            }

            else -> ""
        }
    }
}


fun notifyClientsAboutChanges(tableName: String, changedRecordId: Int) {
    for ((session, loadedIds) in administrationSelectedRowsIds) {
        if (loadedIds.contains(changedRecordId)) {
            CoroutineScope(Dispatchers.IO).launch {
                when (tableName) {
                    "guards" -> {
                        val response = ResponseWithColumn(
                            "guards",
                            DaoMethods.getGuards(loadedIds.toList())
                        )
                        session.send(
                            Frame.Text(
                                Json.encodeToString(
                                    ResponseWithColumn.serializer(GuardInfo.serializer()),
                                    response
                                )
                            )
                        )
                    }

                    "employees" -> {
                        val response = ResponseWithColumn(
                            "employees",
                            DaoMethods.getEmployees(loadedIds.toList())
                        )
                        session.send(
                            Frame.Text(
                                Json.encodeToString(
                                    ResponseWithColumn.serializer(EmployeeInfo.serializer()),
                                    response
                                )
                            )
                        )
                    }

                    "customers" -> {
                        val res = DaoMethods.getCustomers(loadedIds.toList())
                        Json.encodeToString(
                            ListSerializer(CustomerInfo.serializer()),
                            res
                        )
                        val response = ResponseWithColumn(
                            "customers",
                            DaoMethods.getCustomers(loadedIds.toList())
                        )
                        session.send(
                            Frame.Text(
                                Json.encodeToString(
                                    ResponseWithColumn.serializer(CustomerInfo.serializer()),
                                    response
                                )
                            )
                        )
                    }

                    else -> ""
                }
            }
        }
    }
}