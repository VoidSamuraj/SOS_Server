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
        private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        @BeforeClass
        @JvmStatic
        fun setupOnce(){
            CoroutineScope(Dispatchers.IO).launch {
                DaoMethods.addCustomer("login1", "password1", "123456789", "12345678901", "email@test.com")
                DaoMethods.addCustomer("login2", "password2", "987654321", "12345678902", "email2@test.com")
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
        val result = DaoMethods.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        assertTrue(result)
        println("CLIENT "+ DaoMethods.getCustomer(1))

        val report = transaction { Reports.selectAll().singleOrNull() }
        assertNotNull(report)
        assertEquals("Location1", report[Reports.location])
    }

    @Test
    fun `should update report location`() = runTest {
        DaoMethods.addReport(1, "Location1", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        val result = DaoMethods.updateReportLocation(1, "NewLocation")
        assertTrue(result)

        val report = transaction { Reports.selectAll().singleOrNull() }
        assertNotNull(report)
        assertEquals("NewLocation", report[Reports.location])
    }

    @Test
    fun `should change report status`() = runTest {
        DaoMethods.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        val result = DaoMethods.changeReportStatus(1, Report.ReportStatus.IN_PROGRESS)
        assertTrue(result)

        val report =  DaoMethods.getReport(1)
        assertNotNull(report)
        assertEquals(Report.ReportStatus.IN_PROGRESS, report.status)
    }

    @Test
    fun `should delete report`() = runTest {
        DaoMethods.addReport(1, "Location1", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        val result = DaoMethods.deleteReport(1)
        assertTrue(result)

        val report = DaoMethods.getReport(1)
        assertNull(report)
    }

    @Test
    fun `should get all reports`() = runTest {
        DaoMethods.addReport(1, "Location1", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        DaoMethods.addReport(2, "Location2", Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.FINISHED)

        val reports = DaoMethods.getAllReports(page = 1, pageSize = 10)
        assertEquals(2, reports.size)
    }
}
