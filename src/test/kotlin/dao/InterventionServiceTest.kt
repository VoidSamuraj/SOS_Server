package dao
import Interventions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.BeforeClass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InterventionServiceTest {

    companion object{
        private val service = DaoMethods()
        private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        @BeforeClass
        @JvmStatic
        fun setupOnce(){
            CoroutineScope(Dispatchers.IO).launch {
                service.addClient("login1", "password1", "123456789", "12345678901", "email@test.com")
                service.addClient("login2", "password2", "987654321", "12345678902", "email2@test.com")
                service.addGuard("John", "Doe", "123456789")
                service.addGuard("Jane", "Doe", "987654321")
                service.addDispatcher("John", "Doe", "123456789", SystemDispatcher.Role.DISPATCHER)
                service.addDispatcher("Jane", "Doe", "987654321", SystemDispatcher.Role.DISPATCHER)
                service.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
                service.addReport(2, "Location2",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.IN_PROGRESS)
            }
        }
    }
    @BeforeTest
    fun setup() {
        transaction(db) {
            SchemaUtils.drop(Interventions)
            SchemaUtils.create(Interventions)
        }
    }

    @Test
    fun `should add intervention`() = runTest {
        val result = service.addIntervention(1, 1, 1, 101)
        assertTrue(result)

        val intervention = transaction { Interventions.selectAll().singleOrNull() }
        assertNotNull(intervention)
        assertEquals(1, intervention[Interventions.reportId])
        assertEquals(1, intervention[Interventions.guardId])
    }

    @Test
    fun `should get intervention by id`() = runTest {
        service.addIntervention(1, 1, 1, 101)
        val intervention = service.getIntervention(1)
        assertNotNull(intervention)
        assertEquals(1, intervention.report_id)
    }

    @Test
    fun `should get all interventions`() = runTest {
        service.addIntervention(1, 1, 1, 101)
        service.addIntervention(2, 2, 2, 102)

        val interventions = service.getAllInterventions(page = 1, pageSize = 10)
        assertEquals(2, interventions.size)
    }
}
