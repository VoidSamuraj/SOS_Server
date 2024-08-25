package dao
import Interventions
import Reports
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
import kotlin.test.*

class ReportServiceTest {

    companion object{
        private val service = DaoMethods()
        private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        @BeforeClass
        @JvmStatic
        fun setupOnce(){
            CoroutineScope(Dispatchers.IO).launch {
                service.addClient("login1", "password1", "123456789", "12345678901", "email@test.com")
                service.addClient("login2", "password2", "987654321", "12345678902", "email2@test.com")
            }
        }
    }

    @BeforeTest
    fun setup() {
        transaction(db) {
            //Interventions is connected to report need to be removed to freely remove Reports
            SchemaUtils.drop(Interventions,Reports)
            SchemaUtils.create(Reports)
        }
    }

    @Test
    fun `should add report`() = runTest {
        val result = service.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        assertTrue(result)
        println("CLIENT "+ service.getClient(1))

        val report = transaction { Reports.selectAll().singleOrNull() }
        assertNotNull(report)
        assertEquals("Location1", report[Reports.location])
    }

    @Test
    fun `should update report location`() = runTest {
        service.addReport(1, "Location1", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        val result = service.updateReportLocation(1, "NewLocation")
        assertTrue(result)

        val report = transaction { Reports.selectAll().singleOrNull() }
        assertNotNull(report)
        assertEquals("NewLocation", report[Reports.location])
    }

    @Test
    fun `should change report status`() = runTest {
        service.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        val result = service.changeReportStatus(1, Report.ReportStatus.IN_PROGRESS)
        assertTrue(result)

        val report =  service.getReport(1)
        assertNotNull(report)
        assertEquals(Report.ReportStatus.IN_PROGRESS, report.status)
    }

    @Test
    fun `should delete report`() = runTest {
        service.addReport(1, "Location1", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        val result = service.deleteReport(1)
        assertTrue(result)

        val report = service.getReport(1)
        assertNull(report)
    }

    @Test
    fun `should get all reports`() = runTest {
        service.addReport(1, "Location1", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        service.addReport(2, "Location2", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.FINISHED)

        val reports = service.getAllReports(page = 1, pageSize = 10)
        assertEquals(2, reports.size)
    }
}
