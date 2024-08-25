package dao
import Guards
import Interventions
import org.jetbrains.exposed.sql.*
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class GuardServiceTest {

    private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    private val service = DaoMethods()

    @BeforeTest
    fun setup() {
        transaction(db) {
            SchemaUtils.drop(Interventions,Guards)
            SchemaUtils.create(Guards)
        }
    }

    @Test
    fun `should add guard`() = runTest {
        val result = service.addGuard("John", "Doe", "123456789")
        assertTrue(result)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertEquals("John", guard[Guards.name])
    }

    @Test
    fun `should edit guard`() = runTest {
        service.addGuard("John", "Doe", "123456789")
        val result = service.editGuard(1, name = "Jane")
        assertTrue(result)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertEquals("Jane", guard[Guards.name])
    }

    @Test
    fun `should delete guard`() = runTest {
        service.addGuard("John", "Doe", "123456789")
        val result = service.deleteGuard(1)
        assertTrue(result)

        val guard = service.getGuard(1)
        assertNull(guard)
    }

    @Test
    fun `should get guard by id`() = runTest {
        service.addGuard("John", "Doe", "123456789")
        val guard = service.getGuard(1)
        assertNotNull(guard)
        assertEquals("John", guard.name)
    }

    @Test
    fun `should get all guards`() = runTest {
        service.addGuard("John", "Doe", "123456789")
        service.addGuard("Jane", "Doe", "987654321")

        val guards = service.getAllGuards(page = 1, pageSize = 10)
        assertEquals(2, guards.size)
    }
}
