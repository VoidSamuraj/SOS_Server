package plugins

import administrationQueryParams
import administrationSelectedRowsIds
import dao.DaoMethods
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import routes.CustomerField
import routes.EmployeeField
import routes.GuardField


@Serializable
data class QueryParams(
    val table: String,
    val page: Int? = 0,
    val pageSize: Int?= 25,
    val filterColumnName: String? =null,
    val filterOperator: String? =null,
    val filterValue: String? =null,
    val sortColumnName: String? =null,
    val sortOrder: String? = null,

    )

fun Application.configureSockets() {
    install(WebSockets) {
        maxFrameSize = 1024 * 1024
        masking = false
    }
    routing {

        webSocket("/updates") {
            try {
                // Obsługa komunikatów od klienta
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
        }
    }

}
fun loadDataAccordingToParams(session: DefaultWebSocketSession, queryParams: QueryParams) {
    CoroutineScope(Dispatchers.IO).launch {
        val result =
            when (queryParams.table) {
                "employees" -> {
                    val res= DaoMethods.getEmployees(
                        queryParams.page ?: 0,
                        queryParams.pageSize ?: 0,
                        queryParams.filterColumnName?.let { EmployeeField[it] },
                        queryParams.filterValue,
                        queryParams.filterOperator,
                        queryParams.sortColumnName?.let { EmployeeField[it] },
                        queryParams.sortOrder
                    )
                    administrationSelectedRowsIds[session] = res.map { it.id }.toTypedArray()
                    Json.encodeToString(
                        ListSerializer(EmployeeInfo.serializer()),
                        res
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
                    Json.encodeToString(
                        ListSerializer(CustomerInfo.serializer()),
                        res
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

                }

                else -> ""
            }
        session.send(Frame.Text(result))
    }
}


fun notifyClientsAboutChanges(tableName:String, changedRecordId: Int) {
    for ((session, loadedIds) in administrationSelectedRowsIds) {
        if (loadedIds.contains(changedRecordId)) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = when (tableName) {
                    "guards" -> {
                        val res = DaoMethods.getGuards(loadedIds.toList())
                        Json.encodeToString(
                            ListSerializer(GuardInfo.serializer()),
                            res
                        )
                    }

                    "employees" -> {
                        val res = DaoMethods.getEmployees(loadedIds.toList())
                        Json.encodeToString(
                            ListSerializer(EmployeeInfo.serializer()),
                            res
                        )
                    }

                    "customers" -> {
                        val res = DaoMethods.getCustomers(loadedIds.toList())
                        Json.encodeToString(
                            ListSerializer(CustomerInfo.serializer()),
                            res
                        )
                    }

                    else -> ""
                }
                session.send(Frame.Text(result))
            }
        }
    }
}