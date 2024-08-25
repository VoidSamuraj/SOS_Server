package dao

import Interventions
import SystemDispatchers
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class DispatcherServiceTest {

    private  val  db: Database = DatabaseFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    private val service = DaoMethods()

    @BeforeTest
    fun setup() {
        transaction(db) {
            SchemaUtils.drop(Interventions,SystemDispatchers)
            SchemaUtils.create(SystemDispatchers)
        }
    }

    @Test
    fun `should add dispatcher`() = runTest {
        val result = service.addDispatcher("John", "Doe", "123456789", SystemDispatcher.Role.DISPATCHER)
        assertTrue(result)

        val dispatcher = transaction { SystemDispatchers.selectAll().singleOrNull() }
        assertNotNull(dispatcher)
        assertEquals("John", dispatcher[SystemDispatchers.name])
    }

    @Test
    fun `should edit dispatcher`() = runTest {
        service.addDispatcher("John", "Doe", "123456789", SystemDispatcher.Role.DISPATCHER)
        val result = service.editDispatcher(1, name = "Jane")
        assertTrue(result)

        val dispatcher = transaction { SystemDispatchers.selectAll().singleOrNull() }
        assertNotNull(dispatcher)
        assertEquals("Jane", dispatcher[SystemDispatchers.name])
    }

    @Test
    fun `should delete dispatcher`() = runTest {
        service.addDispatcher("John", "Doe", "123456789", SystemDispatcher.Role.DISPATCHER)
        val result = service.deleteDispatcher(1)
        assertTrue(result)

        val dispatcher = service.getDispatcher(1)
        assertNull(dispatcher)
    }

    @Test
    fun `should get dispatcher by id`() = runTest {
        service.addDispatcher("John", "Doe", "123456789", SystemDispatcher.Role.DISPATCHER)
        val dispatcher = service.getDispatcher(1)
        assertNotNull(dispatcher)
        assertEquals("John", dispatcher.name)
    }

    @Test
    fun `should get all dispatchers`() = runTest {
        service.addDispatcher("John", "Doe", "123456789", SystemDispatcher.Role.DISPATCHER)
        service.addDispatcher("Jane", "Doe", "987654321", SystemDispatcher.Role.DISPATCHER)

        val dispatchers = service.getAlDispatchers(page = 1, pageSize = 10)
        assertEquals(2, dispatchers.size)
    }
}
