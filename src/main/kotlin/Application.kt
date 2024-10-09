import dao.DaoMethods
import dao.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plugins.*
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration


val jwtExpirationSeconds=3600L
val administrationQueryParams = mutableMapOf<DefaultWebSocketSession, QueryParams>()
val administrationSelectedRowsIds = mutableMapOf<DefaultWebSocketSession, Array<Int>>()


lateinit var guardTest:List<GuardInfo>
lateinit var guardsFlow:Flow<List<GuardInfo>>
lateinit var reportsFlow:MutableStateFlow<List<Report>>


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init("jdbc:h2:file:./build/db", "org.h2.Driver", "root", "password")
    //DatabaseFactory.init("jdbc:mariadb://localhost:3306/mydatabase", "org.mariadb.jdbc.Driver", "root", "password")
    CoroutineScope(Dispatchers.IO).launch {
        //TEST
        if(DaoMethods.getEmployees(1, 10).isEmpty()) {
            DaoMethods.addEmployee(
                "JanK",
                "qwerty",
                "Jan",
                "Kowalski",
                "123456789",
                "zenusma4@gmail.com",
                Employee.Role.DISPATCHER
            )
            DaoMethods.addEmployee(
                "JanKa",
                "qwerty",
                "Jan",
                "Kowalski",
                "92345657892",
                "dupa@gmail.com",
                Employee.Role.ADMIN
            )
        }
        if(DaoMethods.getGuards(1, 10).isEmpty()) {
            DaoMethods.addGuard("JAN", "qwerty", "Jan", "Pawel", "2221", "jan1@wp.pl")
            DaoMethods.addGuard("JANn", "12we", "Jana", "Pawela", "222137", "john2@wp.pl")
            DaoMethods.addGuard("Miroslaw", "qwerty", "Miroslaw", "Zelent", "221421", "miro@wp.pl")
        }
        if(DaoMethods.getCustomers(1, 10).isEmpty()) {
            DaoMethods.addCustomer("Olorin","qwerty", "Olorin", "MySurname", "1234522", "2137", "lll@ll.pl")
            DaoMethods.addCustomer("Olorin1","qwerty", "Olorin1", "MySurname1","12345222", "21372", "lll@lll.pl")
            DaoMethods.addCustomer("Andrzej","qwerty", "Andrzej", "Andrzej's Surname","15342", "21345672", "andr@lll.pl")
            DaoMethods.addCustomer("Jan","qwerty", "Jan", "Nowak", "13232", "21556372", "jan@lll.pl")
        }

        if(DaoMethods.getReports(1, 10).isEmpty()) {
            DaoMethods.addReport(1,"{lat: 52.2297, lng: 21.0122 }",  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.WAITING)
            DaoMethods.addReport(1,"{lat: 50.0647, lng: 19.9450 }",  Clock.System.now().plus(2.toDuration(DurationUnit.SECONDS)).toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.WAITING)
            DaoMethods.addReport(2,"{lat: 54.3520, lng: 18.6466 }",  Clock.System.now().minus(2.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.IN_PROGRESS)
            DaoMethods.addReport(3,"{lat: 51.1079, lng: 17.0385 }",  Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()), Report.ReportStatus.WAITING)
        }
        println(DaoMethods.getReports(1, 10))
        if(DaoMethods.getInterventions(1, 10).isEmpty()) {
            DaoMethods.addIntervention(3, 3,2,
                Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()),
                Intervention.InterventionStatus.FINISHED)
            DaoMethods.addIntervention(1, 1,1,
                Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()),
                Intervention.InterventionStatus.FINISHED)
            DaoMethods.addIntervention(2, 2,2,
                Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().minus(5.toDuration(DurationUnit.MINUTES)).toLocalDateTime(TimeZone.currentSystemDefault()),
                Intervention.InterventionStatus.FINISHED)
        }

        fun randomLocationInPoland(): String {
            val lat = Random.nextDouble(49.0, 54.83)
            val lng = Random.nextDouble(14.12, 24.15)
            return "{lat: $lat, lng: $lng}"
        }


        guardTest= runBlocking{DaoMethods.getAllGuards()}

        //END TEST


        reportsFlow = MutableStateFlow<List<Report>>(DaoMethods.getAllReports(true))
        guardsFlow = flow{

            //TEST
            var randomNumber:Int
            var newLocation: String
            repeat (100){
                if(guardTest.isNotEmpty()) {
                    randomNumber = Random.nextInt(0, guardTest.size)
                    newLocation = randomLocationInPoland()
                    emit(listOf(guardTest[randomNumber].apply {statusCode=Random.nextInt(0, 2); location = newLocation }))
                }
                delay(2000)
            }
            //END TEST
        }
    }

    configureWorkerAuthentication()
    configureSerialization()
    configureSession()
    configureSockets()
    configureHTTP()
    configureMonitoring()
    configureRouting()

}
