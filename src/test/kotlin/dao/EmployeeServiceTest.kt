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
        val result = DaoMethods.addEmployee("JonnnD", "zaq1@WSX","John", "Doe", "123456789", Employee.Role.DISPATCHER)
        assertTrue(result.first)

        val employee = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(employee)
        assertEquals("John", employee[Employees.name])
    }
    @Test
    fun `should add only three employees`() = runTest {
        var result = DaoMethods.addEmployee("JonnnD", "zaq1@WSX","John", "Doe", "123456789", Employee.Role.DISPATCHER)
        assertTrue(result.first)

        result = DaoMethods.addEmployee("JonnnD", "zaq1@WSX2","John2", "Doe2", "1234567892", Employee.Role.DISPATCHER)
        assertFalse(result.first)

        result = DaoMethods.addEmployee("JonnnD2", "zaq1@WSX","John2", "Doe2", "1234567892", Employee.Role.DISPATCHER)
        assertTrue(result.first)

        result = DaoMethods.addEmployee("JonnnD3", "zaq1@WSX","John", "Doe3", "1234567893", Employee.Role.DISPATCHER)
        assertTrue(result.first)

        result = DaoMethods.addEmployee("JonnnD3", "zaq1@WSX","John3", "Doe", "1234567893", Employee.Role.DISPATCHER)
        assertFalse(result.first)

        result = DaoMethods.addEmployee("JonnnD3", "zaq1@WSX3","John3", "Doe3", "123456789", Employee.Role.DISPATCHER)
        assertFalse(result.first)
    }

    @Test
    fun `should edit employee`() = runTest {
        DaoMethods.addEmployee("JonnnD", "zaq1@WSX", "John", "Doe", "123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.editEmployee(1, name = "Jane", password = "zaq1@WSX")
        assertTrue(result.first)

        val employee = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(employee)
        assertEquals("Jane", employee[Employees.name])
    }
    @Test
    fun `should not edit employee`() = runTest {
        DaoMethods.addEmployee("JonnnD", "zaq1@WSX", "John", "Doe", "123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.editEmployee(1, name = "Jane", password = "qwerty")
        assertFalse(result.first)

        val employee = transaction { Employees.selectAll().singleOrNull() }
        assertNotNull(employee)
        assertNotEquals("Jane", employee[Employees.name])
    }
    @Test
    fun `should delete employee`() = runTest {
        DaoMethods.addEmployee("JonnnD", "zaq1@WSX", "John", "Doe", "123456789", Employee.Role.DISPATCHER)
        val result = DaoMethods.deleteEmployee(1)
        assertTrue(result)

        val employee = DaoMethods.getEmployee(1)
        assertNotNull(employee)
        assertTrue(employee.account_deleted == true)
    }

    @Test
    fun `should get employee by id`() = runTest {
        DaoMethods.addEmployee("JonnnD", "zaq1@WSX", "John", "Doe", "123456789", Employee.Role.DISPATCHER)
        val employee = DaoMethods.getEmployee(1)
        assertNotNull(employee)
        assertEquals("John", employee.name)
    }

    @Test
    fun `should get all employees`() = runTest {
        DaoMethods.addEmployee("JonnnD", "zaq1@WSX", "John", "Doe", "123456987", Employee.Role.DISPATCHER)
        DaoMethods.addEmployee("JaneD", "zaq1@WSX", "Jane", "Doe", "123456789", Employee.Role.DISPATCHER)

        val employees = DaoMethods.getAllEmployees(page = 1, pageSize = 10)
        assertEquals(2, employees.size)
    }
}
