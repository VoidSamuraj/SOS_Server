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
    fun `should add employee`() = runTest {
        val result = DaoMethods.addEmployee("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        assertTrue(result)

        val employee = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(employee)
        assertEquals("John", employee[Employees.name])
    }

    @Test
    fun `should edit employee`() = runTest {
        DaoMethods.addEmployee("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.editEmployee(1, name = "Jane")
        assertTrue(result)

        val employee = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(employee)
        assertEquals("Jane", employee[Employees.name])
    }

    @Test
    fun `should delete employee`() = runTest {
        DaoMethods.addEmployee("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.deleteEmployee(1)
        assertTrue(result)

        val employee = DaoMethods.getEmployee(1)
        assertNull(employee)
    }

    @Test
    fun `should get employee by id`() = runTest {
        DaoMethods.addEmployee("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        val employee = DaoMethods.getEmployee(1)
        assertNotNull(employee)
        assertEquals("John", employee.name)
    }

    @Test
    fun `should get all employees`() = runTest {
        DaoMethods.addEmployee("John", "Doe", "qwerty","123456789", Employee.Role.DISPATCHER)
        DaoMethods.addEmployee("Jane", "Doe", "qwerty","987654321", Employee.Role.DISPATCHER)

        val employees = DaoMethods.getAllEmployees(page = 1, pageSize = 10)
        assertEquals(2, employees.size)
    }
}
