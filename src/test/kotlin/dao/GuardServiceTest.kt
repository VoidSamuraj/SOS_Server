package dao
import Guards
import Interventions
import org.jetbrains.exposed.sql.*
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class GuardServiceTest {

    private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")

    @BeforeTest
    fun setup() {
        transaction(db) {
            SchemaUtils.drop(Interventions,Guards)
            SchemaUtils.create(Guards)
        }
    }

    @Test
    fun `should add guard`() = runTest {
        val result = DaoMethods.addGuard("John", "Doe", "123456789")
        assertTrue(result)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertEquals("John", guard[Guards.name])
    }

    @Test
    fun `should edit guard`() = runTest {
        DaoMethods.addGuard("John", "Doe", "123456789")
        val result = DaoMethods.editGuard(1, name = "Jane")
        assertTrue(result)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertEquals("Jane", guard[Guards.name])
    }

    @Test
    fun `should delete guard`() = runTest {
        DaoMethods.addGuard("John", "Doe", "123456789")
        val result = DaoMethods.deleteGuard(1)
        assertTrue(result)

        val guard = DaoMethods.getGuard(1)
        assertNull(guard)
    }

    @Test
    fun `should get guard by id`() = runTest {
        DaoMethods.addGuard("John", "Doe", "123456789")
        val guard = DaoMethods.getGuard(1)
        assertNotNull(guard)
        assertEquals("John", guard.name)
    }

    @Test
    fun `should get all guards`() = runTest {
        DaoMethods.addGuard("John", "Doe", "123456789")
        DaoMethods.addGuard("Jane", "Doe", "987654321")

        val guards = DaoMethods.getAllGuards(page = 1, pageSize = 10)
        assertEquals(2, guards.size)
    }
}
