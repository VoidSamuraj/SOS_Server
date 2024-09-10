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
        val result = DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        assertTrue(result.first)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertEquals("John", guard[Guards.name])
    }
    @Test
    fun `should add only three guards`() = runTest {
        var result = DaoMethods.addGuard("JonnnD", "zaq1@WSX","John", "Doe", "123456789")
        assertTrue(result.first)

        result = DaoMethods.addGuard("JonnnD", "zaq1@WSX2","John2", "Doe2", "1234567892")
        assertFalse(result.first)

        result = DaoMethods.addGuard("JonnnD2", "zaq1@WSX","John2", "Doe2", "1234567892")
        assertTrue(result.first)

        result = DaoMethods.addGuard("JonnnD3", "zaq1@WSX","John", "Doe3", "1234567893")
        assertTrue(result.first)

        result = DaoMethods.addGuard("JonnnD3", "zaq1@WSX","John3", "Doe", "1234567893")
        assertFalse(result.first)

        result = DaoMethods.addGuard("JonnnD3", "zaq1@WSX3","John3", "Doe3", "123456789")
        assertFalse(result.first)
    }

    @Test
    fun `should edit guard`() = runTest {
        DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        val result = DaoMethods.editGuard(1, name = "Jane", password = "zaq1@WSX")
        assertTrue(result.first)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertEquals("Jane", guard[Guards.name])
    }

    @Test
    fun `should not edit guard`() = runTest {
        DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        val result = DaoMethods.editGuard(1, name = "Jane", password = "qwerty")
        assertFalse(result.first)

        val guard = transaction { Guards.selectAll().singleOrNull() }
        assertNotNull(guard)
        assertNotEquals("Jane", guard[Guards.name])
    }

    @Test
    fun `should delete guard`() = runTest {
        DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        val result = DaoMethods.deleteGuard(1)
        assertTrue(result)
        val guard = DaoMethods.getGuard(1)
        assertNotNull(guard)
        assertTrue(guard.account_deleted == true)
    }

    @Test
    fun `should get guard by id`() = runTest {
        DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        val guard = DaoMethods.getGuard(1)
        assertNotNull(guard)
        assertEquals("John", guard.name)
    }

    @Test
    fun `should get guard by login & password`() = runTest {
        DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        val guard = DaoMethods.getGuard("JonnnD", "zaq1@WSX")
        assertNotNull(guard.second)
        assertEquals("John", guard.second!!.name)
    }

    @Test
    fun `should get all guards`() = runTest {
        DaoMethods.addGuard("JonnnD", "zaq1@WSX", "John", "Doe", "123456789")
        DaoMethods.addGuard("JaneD", "zaq1@WSX", "Jane", "Doe", "987654321")

        val guards = DaoMethods.getGuards(page = 1, pageSize = 10)
        assertEquals(2, guards.size)
    }
}
