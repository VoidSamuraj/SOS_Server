import dao.DaoMethods
import dao.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.dto.GuardInfo
import models.entity.Employee
import models.entity.Intervention
import models.entity.Report
import plugins.*
import security.Keys
import viewmodel.SecurityDataViewModel
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration


val jwtExpirationSeconds=3600L
val administrationQueryParams = mutableMapOf<DefaultWebSocketSession, QueryParams>()
val administrationSelectedRowsIds = mutableMapOf<DefaultWebSocketSession, Array<Int>>()

lateinit var guardTest:List<GuardInfo>

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    DatabaseFactory.init(
        jdbcURL = dbAddress,
        driverClassName = "org.postgresql.Driver",
        user = Keys.dbLogin,
        password = Keys.dbPassword
    )

    CoroutineScope(Dispatchers.IO).launch {
        //TEST
        if(DaoMethods.getEmployees(1, 10).isEmpty()) {
            DaoMethods.addEmployee(
                "JanK",
                "qwerty",
                "Jan",
                "Kowalski",
                "123456789",
                "jannow@gmail.com",
                Employee.Role.DISPATCHER
            )
            DaoMethods.addEmployee(
                "JanKa",
                "qwerty",
                "Jan",
                "Kowalski",
                "92345657892",
                "JohnPaul@gmail.com",
                Employee.Role.ADMIN
            )
        }
        if(DaoMethods.getGuards(1, 10).isEmpty()) {
            DaoMethods.addGuard("JAN", "qwerty", "Jan", "Kowalski", "2221", "jan1@wp.pl")
                DaoMethods.addGuard("Miroslaw", "qwerty", "Miroslaw", "Zelent", "221421", "miro@wp.pl")
        }
        if(DaoMethods.getCustomers(1, 10).isEmpty()) {
            DaoMethods.addCustomer("Andrzej","qwerty", "Andrzej", "Kowalski", "1234522", "2137", "lll@ll.pl")
            DaoMethods.addCustomer("Jan","qwerty", "Jan", "Nowak", "13232", "21556372", "jan@lll.pl")
        }

        if(DaoMethods.getReports(1, 10).isEmpty()) {
            DaoMethods.addReport(1,"{lat: 52.2297, lng: 21.0122 }",  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.WAITING)
            DaoMethods.addReport(1,"{lat: 50.0647, lng: 19.9450 }",  Clock.System.now().plus(2.toDuration(DurationUnit.SECONDS)).toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.WAITING)
            DaoMethods.addReport(2,"{lat: 54.3520, lng: 18.6466 }",  Clock.System.now().minus(2.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.IN_PROGRESS)
            DaoMethods.addReport(3,"{lat: 51.1079, lng: 17.0385 }",  Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.WAITING)
        }

        fun randomLocationInPoland(): String {
            val lat = Random.nextDouble(49.0, 54.83)
            val lng = Random.nextDouble(14.12, 24.15)
            return "{lat: $lat, lng: $lng}"
        }


        guardTest= runBlocking{DaoMethods.getAllGuards()}
        guardTest.forEach() {it->
            it.location=randomLocationInPoland()
            it.statusCode =Random.nextInt(0, 2)
        }
        //END TEST

        SecurityDataViewModel.setReports(DaoMethods.getAllReports(true))
        SecurityDataViewModel.setGuards(guardTest)
    }

    configureWorkerAuthentication()
    configureSerialization()
    configureSession()
    configureSockets()
    configureHTTPS()
    configureMonitoring()
    configureRouting()

}
