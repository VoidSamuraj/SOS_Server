package plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

fun Application.configureSockets() {
    val webSocketHandler = WebSocketHandler()
    webSocketHandler.startDataUpdateListeners()

    CoroutineScope(Dispatchers.Default).launch{
        var c=0
        while (true) {
               delay(2000) // Simulate delay
                val ran= (Math.random()*3).toInt()
                when(ran){
                    0 -> webSocketHandler.updateCustomers("CUSTOMER ${c}")
                    1 -> webSocketHandler.updateEmployees("EMPLOYEE ${c}")
                    2 -> webSocketHandler.updateGuards("GUARD ${c}")
                }
            }
    }


    install(WebSockets) {
        //pingPeriod = Duration.ofSeconds(15)
       // timeout = Duration.ofSeconds(15)
        maxFrameSize = 1024 * 1024
        masking = true
    }
    routing {
       /* webSocket("/ws") { // websocketSession
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }*/

        webSocket("/updates") {
            webSocketHandler.handleWebSocket(this)
            /*
            val session = this
            val dataUpdates = Channel<String>(Channel.UNLIMITED)
            // Send an initial message to the client
            session.send(Frame.Text("sd"))

            // Launch a coroutine to simulate data changes
            launch {
                while (true) {
                    delay(1000) // Simulate delay
                    tableData.add("New Data at ${System.currentTimeMillis()}")
                    dataUpdates.send(tableData.joinToString(", "))
                }
            }

            // Launch a coroutine to send updates to the client
            launch {
                for (update in dataUpdates) {
                    session.send(Frame.Text(update))
                }
            }
            */
        }

    }

}



class WebSocketHandler {
    private val clients = CopyOnWriteArrayList<WebSocketSession>()

    // Trzy tabele danych
    private val customers = mutableListOf("Table1 Data 1", "Table1 Data 2")
    private val employees = mutableListOf("Table2 Data 1", "Table2 Data 2")
    private val guards = mutableListOf("Table3 Data 1", "Table3 Data 2")

    // Trzy kanały do obsługi aktualizacji danych z każdej tabeli
    private val customersUpdates = Channel<Unit>(Channel.UNLIMITED)
    private val employeesUpdates = Channel<Unit>(Channel.UNLIMITED)
    private val guardsUpdates = Channel<Unit>(Channel.UNLIMITED)

    suspend fun handleWebSocket(session: WebSocketServerSession) {
        clients.add(session)

        // Nasłuchuj na zamknięcie połączenia, aby usunąć klienta z listy
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    when (text) {
                        "getCustomers" -> sendDataToClient(session, customers)
                        "getEmployees" -> sendDataToClient(session, employees)
                        "getGuards" -> sendDataToClient(session, guards)
                    }
                }
            }
        } finally {
            clients.remove(session)
        }
    }

    // Funkcja do wysłania danych z konkretnej tabeli do pojedynczego klienta
    private suspend fun sendDataToClient(session: WebSocketSession, data: List<String>) {
        val jsonData = Json.encodeToString(String.serializer(), data.toString())
        println("Wysyłane dane: $jsonData")
        session.send(Frame.Text(jsonData))
    }

    // Funkcja do wysyłania danych do wszystkich klientów z danej tabeli
    private suspend fun broadcastData(data: List<String>) {
        val jsonData = Json.encodeToString(String.serializer(), data.toString())
        clients.forEach { client ->
            if (client.isActive) {
                client.send(Frame.Text(jsonData))
            } else {
                clients.remove(client)
            }
        }
    }

    // Funkcja do aktualizacji danych i wysłania notyfikacji o zmianie z danej tabeli
    suspend fun updateCustomers(newData: String) {
        customers.add(newData)
        customersUpdates.send(Unit)
    }

    suspend fun updateEmployees(newData: String) {
        employees.add(newData)
        employeesUpdates.send(Unit)
    }

    suspend fun updateGuards(newData: String) {
        guards.add(newData)
        guardsUpdates.send(Unit)
    }

    // Inicjalizacja obsługi zmian danych w każdej tabeli
    fun startDataUpdateListeners() {
        CoroutineScope(Dispatchers.Default).launch {
            for (change in customersUpdates) {
                broadcastData(customers)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            for (change in employeesUpdates) {
                broadcastData(employees)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            for (change in guardsUpdates) {
                broadcastData(guards)
            }
        }
    }
}
