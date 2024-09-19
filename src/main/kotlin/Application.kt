import dao.DaoMethods
import dao.DatabaseFactory
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import plugins.*


val jwtExpirationSeconds=3600L
lateinit var guards: List<GuardInfo>

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init("jdbc:h2:file:./build/db", "org.h2.Driver", "root", "password")

    //TEST
    CoroutineScope(Dispatchers.IO).launch {
        if(DaoMethods.getEmployees(1, 10).isEmpty())
            DaoMethods.addEmployee("JanK", "qwerty", "Jan", "Kowalski", "123456789","zenusma4@gmail.com",  Employee.Role.DISPATCHER)
        if(DaoMethods.getGuards(1, 10).isEmpty()) {
            DaoMethods.addGuard("JAN", "qwerty", "Jan", "Pawel", "2221")
            DaoMethods.addGuard("JANn", "12we", "Jana", "Pawela", "222137")
            DaoMethods.addGuard("Miroslaw", "qwerty", "Miroslaw", "Zelent", "221421")
        }
        if(DaoMethods.getCustomers(1, 10).isEmpty()) {
            DaoMethods.addCustomer("Olorin","qwerty","1234522", "2137", "lll@ll.pl")
        }
        guards=DaoMethods.getAllGuards()
    }

    configureWorkerAuthentication()
    configureSerialization()
    configureSession()
    configureSockets()
    configureHTTP()
    configureMonitoring()
    configureRouting()
}
