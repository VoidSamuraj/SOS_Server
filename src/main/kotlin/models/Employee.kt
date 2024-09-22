import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table


@Serializable
data class EmployeeInfo(val id:Int, val name: String, val surname: String, val phone: String, val email:String, val roleCode:Short, val account_deleted:Boolean): Principal

@Serializable
data class Employee(val id:Int, val login: String, val password:String, val name: String, val surname: String, val phone: String, val email:String, val roleCode:Short, val account_deleted:Boolean): Principal{
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

    fun toEmployeeInfo():EmployeeInfo{
        return EmployeeInfo(id,name,surname,phone,email,roleCode,account_deleted)
    }
}

object Employees : Table() {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 20).uniqueIndex()
    val password = varchar("password", 60)
    val name = varchar("name",40)
    val surname = varchar("surname", 40)
    val phone =  varchar("phone", 20).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val role = short("role")
    val account_deleted=bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}