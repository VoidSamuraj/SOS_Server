import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class SystemDispatcher(val id:Int,val name: String, val surname: String, val password: String,val phone: String, val roleCode:Short): Principal{
    /**
     * Enum representing role of worker.
     *
     * Contains 3 Roles: [Role.DISPATCHER], [Role.MANAGER], [Role.ADMIN]
     */
    enum class Role(val role:Int) {
        DISPATCHER(0),
        MANAGER(1),
        ADMIN(2);

        companion object {
            /**
             * Convert int to class instance.
             *
             * @param value value of enum
             * @throws IllegalArgumentException if enum not contains object with such number
             */
            fun fromInt(value: Int) = entries.firstOrNull { it.role == value }
                ?: throw IllegalArgumentException("Unknown ReportStatus for value: $value")
        }
    }

    val role: Role
        get() = Role.fromInt(roleCode.toInt())
}

object SystemDispatchers : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name",40)
    val surname = varchar("surname", 40)
    val password = varchar("password", 40)
    val phone =  varchar("phone", 20).uniqueIndex()
    val role = short("role")
    override val primaryKey = PrimaryKey(id)
}