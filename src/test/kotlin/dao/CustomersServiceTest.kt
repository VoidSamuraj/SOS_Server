import dao.DaoMethods
import dao.DatabaseFactory
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class CustomersServiceTest {

    private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    @BeforeTest
    fun setup() {
        transaction(db) {
            //clean and recreate database before each test (in other case ids of records are different)
            SchemaUtils.drop(Interventions,Reports,Customers)
            SchemaUtils.create(Customers)
        }
    }

    @Test
    fun `should add customer`() = runTest {
        val result = DaoMethods.addCustomer("login1", "password1","Name1", "Surname1", "123456789", "12345678901", "email@test.com")
        assertTrue(result.first)

        val customer = transaction { Customers.selectAll().singleOrNull() }
        assertNotNull(customer)
        assertEquals("login1", customer[Customers.login])
    }

    @Test
    fun `should add only two customer`() = runTest {
        var result = DaoMethods.addCustomer("login1", "password1","Name0", "Surname0", "123456789", "12345678901", "email@test.com")
        assertTrue(result.first)
        result = DaoMethods.addCustomer("login1", "password1","Name1", "Surname1", "1234567892", "123456789012", "email@test2.com")
        assertFalse(result.first)
        result = DaoMethods.addCustomer("login2", "password1","Name2", "Surname2", "1234567892", "123456789012", "email@test2.com")
        assertTrue(result.first)
        result = DaoMethods.addCustomer("login3", "password1","Name3", "Surname3", "1234567892", "123456789013", "email@test3.com")
        assertFalse(result.first)
        result = DaoMethods.addCustomer("login3", "password1","Name4", "Surname4", "1234567893", "123456789012", "email@test3.com")
        assertFalse(result.first)
        result = DaoMethods.addCustomer("login3", "password1","Name5", "Surname5", "1234567893", "123456789013", "email@test2.com")
        assertFalse(result.first)
    }

    @Test
    fun `should edit customer`() = runTest {
        DaoMethods.addCustomer("login2", "password2","Name2", "Surname2", "987654321", "12345678902", "email2@test.com")

        val result = DaoMethods.editCustomer(id = 1, login = "newLogin", password = "password2", null, null, null)
        assertTrue(result.first)

        val customer = transaction { Customers.selectAll().singleOrNull() }
        assertNotNull(customer)
        assertEquals("newLogin", customer[Customers.login])
    }
    @Test
    fun `should not edit customer`() = runTest {
        DaoMethods.addCustomer("login2", "password2","Name2", "Surname2", "987654321", "12345678902", "email2@test.com")

        val result = DaoMethods.editCustomer(id = 1, login = "newLogin", password = "password1", null, null, null)
        assertFalse(result.first)

        val customer = transaction { Customers.selectAll().singleOrNull() }
        assertNotNull(customer)
        assertNotEquals("newLogin", customer[Customers.login])
    }
    @Test
    fun `should delete customer`() = runTest {
        DaoMethods.addCustomer("login3", "password3","Name3", "Surname3", "555555555", "12345678903", "email3@test.com")
        val result = DaoMethods.deleteCustomer(1)
        assertTrue(result)
        val customer = DaoMethods.getCustomer(1)
        assertNotNull(customer)
        assertTrue(customer.account_deleted == true)
    }

    @Test
    fun `should get customer by id`() = runTest {
        DaoMethods.addCustomer("login4", "password4","Name4", "Surname4", "444444444", "12345678904", "email4@test.com")
        val customer = DaoMethods.getCustomer(1)
        assertNotNull(customer)
        assertEquals("email4@test.com", customer.email)
    }
    @Test
    fun `should get customer by login & password`() = runTest {
        DaoMethods.addCustomer("Jonnn", "zaq1@WSX","John", "Surname", "123456789", "1234456545", "JonnnD@wp.pl")
        val customer = DaoMethods.getCustomer("Jonnn", "zaq1@WSX")
        print("CLIENTT "+customer.first)
        assertNotNull(customer.second)
        assertEquals("JonnnD@wp.pl", customer.second!!.email)
    }

    @Test
    fun `should get all customers`() = runTest {
        DaoMethods.addCustomer("login5", "password5","Name5", "Surname5", "555555555", "12345678905", "email5@test.com")
        DaoMethods.addCustomer("login6", "password6","Name6", "Surname6", "666666666", "12345678906", "email6@test.com")
        val customers = DaoMethods.getCustomers(page = 1, pageSize = 10).toList()
        assertEquals(2, customers.size)
    }

}