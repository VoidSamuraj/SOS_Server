package dao
import Interventions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.entity.Employee
import models.entity.Intervention
import models.entity.Report
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class InterventionServiceTest {

    companion object{
        private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    }
    @BeforeTest
    fun setup() {
        transaction(db) {
            SchemaUtils.drop(Interventions)
            SchemaUtils.create(Interventions)
            runBlocking{
                DaoMethods.addCustomer("login1", "password1","Name1", "Surname1", "123456789", "12345678901", "email@test.com")
                DaoMethods.addCustomer("login2", "password2","Name2", "Surname2", "987654321", "12345678902", "email2@test.com")
                DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789", "johnD1@wp.pl")
                DaoMethods.addGuard("JaneD", "zaq1@WSX","Jane", "Doe", "987654321", "johJaneD1@wp.pl")
                DaoMethods.addEmployee("JonnnD", "zaq1@WSX", "John", "Doe", "123456789","John@gmail.com", Employee.Role.DISPATCHER)
                DaoMethods.addEmployee("JaneD", "zaq1@WSX","Jane", "Doe", "987654321","Jane@gmail.com", Employee.Role.DISPATCHER)
                DaoMethods.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
                DaoMethods.addReport(2, "Location2",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.IN_PROGRESS)
            }
        }
    }

    @Test
    fun `should add intervention`() = runTest {
        val result = DaoMethods.addIntervention(1, 1, 1, Clock.System.now().toLocalDateTime(TimeZone.UTC),  Clock.System.now().plus(1.hours).toLocalDateTime(TimeZone.UTC), Intervention.InterventionStatus.FINISHED )
        assertTrue(result)

        val intervention = transaction { Interventions.selectAll().singleOrNull() }
        assertNotNull(intervention)
        assertEquals(1, intervention[Interventions.report_id])
        assertEquals(1, intervention[Interventions.guard_id])
    }

    @Test
    fun `should get intervention by id`() = runTest {
        DaoMethods.addIntervention(1, 1, 1, Clock.System.now().toLocalDateTime(TimeZone.UTC),  Clock.System.now().plus(1.hours).toLocalDateTime(TimeZone.UTC), Intervention.InterventionStatus.FINISHED)
        val intervention = DaoMethods.getIntervention(1)
        assertNotNull(intervention)
        assertEquals(1, intervention.report_id)
    }

    @Test
    fun `should get all interventions`() = runTest {
        DaoMethods.addIntervention(1, 1, 1, Clock.System.now().toLocalDateTime(TimeZone.UTC),  Clock.System.now().plus(1.hours).toLocalDateTime(TimeZone.UTC), Intervention.InterventionStatus.FINISHED)
        DaoMethods.addIntervention(2, 2, 2, Clock.System.now().toLocalDateTime(TimeZone.UTC),  Clock.System.now().plus(1.hours).toLocalDateTime(TimeZone.UTC), Intervention.InterventionStatus.FINISHED)

        val interventions = DaoMethods.getInterventions(page = 1, pageSize = 10)
        assertEquals(2, interventions.size)
    }
}
