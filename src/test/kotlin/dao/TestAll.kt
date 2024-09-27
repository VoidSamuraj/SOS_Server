package dao
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import kotlin.test.assertTrue

class TestAll {

    @Test
    fun runAllTests() {
        val result: Result = JUnitCore.runClasses(
            CustomersServiceTest::class.java,
            EmployeeServiceTest::class.java,
            GuardServiceTest::class.java,
            ReportServiceTest::class.java,
            InterventionServiceTest::class.java
        )

        println("Testy zakończone z wynikiem: ${result.runCount} uruchomionych, ${result.failureCount} niepowodzeń.")


        assertTrue(result.wasSuccessful(), "Nie wszystkie testy zakończyły się powodzeniem")
    }
}