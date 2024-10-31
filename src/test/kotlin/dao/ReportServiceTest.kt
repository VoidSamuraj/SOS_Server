package dao
import Interventions
import Reports
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.entity.Report
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class ReportServiceTest {

    companion object{
        private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    }

    @BeforeTest
    fun setup() {
        transaction(db) {
            //Interventions is connected to report need to be removed to freely remove Reports
            SchemaUtils.drop(Interventions,Reports)
            SchemaUtils.create(Reports)
            runBlocking{
                    DaoMethods.addCustomer("login1", "password1","Name1", "Surname1", "123456789", "12345678901", "email@test.com")
                    DaoMethods.addCustomer("login2", "password2","Name2", "Surname2", "987654321", "12345678902", "email2@test.com")
            }
        }
    }

    @Test
    fun `should add report`() = runTest {
        DaoMethods.addCustomer("login1", "password1","Name1", "Surname1", "123456789", "12345678901", "email@test.com")
        val result = DaoMethods.addReport(1, "Location1",  Clock.System.now().toLocalDateTime(TimeZone.UTC), Report.ReportStatus.WAITING)
        assertTrue(result!=-1)

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
        assertNotNull(result)

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

        val reports = DaoMethods.getReports(page = 1, pageSize = 10)
        assertEquals(2, reports.size)
    }
}
