import dao.DaoMethods
import dao.DatabaseFactory
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import plugins.*


val jwtExpirationSeconds=3600L

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init("jdbc:h2:file:./build/db", "org.h2.Driver", "root", "password")
    CoroutineScope(Dispatchers.IO).launch {
        if(DaoMethods.getAlDispatchers(1, 10).isEmpty())
            DaoMethods.addDispatcher("Jan", "Kowalski", "qwerty","123456789", Employee.Role.DISPATCHER)
    }
    //configureTemplating()
    configureSockets()
    configureHTTP()
    configureSecurity()
    configureMonitoring()
    configureRouting()
    configureSerialization()
}
