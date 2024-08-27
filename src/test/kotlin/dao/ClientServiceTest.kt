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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ClientServiceTest {

    private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    @BeforeTest
    fun setup() {
        transaction(db) {
            //clean and recreate database before each test (in other case ids of records are different)
            SchemaUtils.drop(Interventions,Reports,SystemClients)
            SchemaUtils.create(SystemClients)
        }
    }

    @Test
    fun `should add client`() = runTest {
        val result = DaoMethods.addClient("login1", "password1", "123456789", "12345678901", "email@test.com")
        assertTrue(result)

        val client = transaction { SystemClients.selectAll().singleOrNull() }
        assertNotNull(client)
        assertEquals("login1", client[SystemClients.login])
    }

    @Test
    fun `should edit client`() = runTest {
        DaoMethods.addClient("login2", "password2", "987654321", "12345678902", "email2@test.com")

        println("clients "+DaoMethods.getAllClients(1,10))
        val result = DaoMethods.editClient(1, "newLogin", null, null, null, null)
        assertTrue(result)

        val client = transaction { SystemClients.selectAll().singleOrNull() }
        assertNotNull(client)
        assertEquals("newLogin", client[SystemClients.login])
    }

    @Test
    fun `should delete client`() = runTest {
        DaoMethods.addClient("login3", "password3", "555555555", "12345678903", "email3@test.com")
        val result = DaoMethods.deleteClient(1)
        assertTrue(result)
        val client = DaoMethods.getClient(1)
        print("Clients "+client)
        assertNotNull(client)
        assertTrue(client.account_deleted == true)
    }

    @Test
    fun `should get client by id`() = runTest {
        DaoMethods.addClient("login4", "password4", "444444444", "12345678904", "email4@test.com")
        val client = DaoMethods.getClient(1)
        assertNotNull(client)
        assertEquals("login4", client.login)
    }

    @Test
    fun `should get all clients`() = runTest {
        DaoMethods.addClient("login5", "password5", "555555555", "12345678905", "email5@test.com")
        DaoMethods.addClient("login6", "password6", "666666666", "12345678906", "email6@test.com")
        val clients = DaoMethods.getAllClients(page = 1, pageSize = 10).toList()
        assertEquals(2, clients.size)
    }

}