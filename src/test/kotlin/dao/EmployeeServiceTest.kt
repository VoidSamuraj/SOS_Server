package dao

import Interventions
import Employees
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class EmployeeServiceTest {

    private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")

    @BeforeTest
    fun setup() {
        transaction(db) {
            SchemaUtils.drop(Interventions,Employees)
            SchemaUtils.create(Employees)
        }
    }

    @Test
    fun `should add dispatcher`() = runTest {
        val result = DaoMethods.addDispatcher("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        assertTrue(result)

        val dispatcher = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(dispatcher)
        assertEquals("John", dispatcher[Employees.name])
    }

    @Test
    fun `should edit dispatcher`() = runTest {
        DaoMethods.addDispatcher("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.editDispatcher(1, name = "Jane")
        assertTrue(result)

        val dispatcher = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(dispatcher)
        assertEquals("Jane", dispatcher[Employees.name])
    }

    @Test
    fun `should delete dispatcher`() = runTest {
        DaoMethods.addDispatcher("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.deleteDispatcher(1)
        assertTrue(result)

        val dispatcher = DaoMethods.getDispatcher(1)
        assertNull(dispatcher)
    }

    @Test
    fun `should get dispatcher by id`() = runTest {
        DaoMethods.addDispatcher("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        val dispatcher = DaoMethods.getDispatcher(1)
        assertNotNull(dispatcher)
        assertEquals("John", dispatcher.name)
    }

    @Test
    fun `should get all dispatchers`() = runTest {
        DaoMethods.addDispatcher("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        DaoMethods.addDispatcher("Jane", "Doe", "qwerty","987654321", Employee.Role.DISPATCHER)

        val dispatchers = DaoMethods.getAlDispatchers(page = 1, pageSize = 10)
        assertEquals(2, dispatchers.size)
    }
}
