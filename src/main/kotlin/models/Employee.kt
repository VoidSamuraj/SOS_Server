import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * This class represents a simplified version of an Employee, including their personal information
 * such as contact details and role.
 *
 * @property id Unique identifier for the employee.
 * @property name First name of the employee.
 * @property surname Last name of the employee.
 * @property phone Contact phone number of the employee.
 * @property email Email address of the employee.
 * @property roleCode Role code representing the employee's role, which can be one of the following:
 * [Employee.Role.DISPATCHER], [Employee.Role.MANAGER], or [Employee.Role.ADMIN].
 * @property account_deleted Indicates whether the employee's account has been deleted.
 *
 * @constructor Creates an EmployeeInfo instance with the specified details.
 */
@Serializable
data class EmployeeInfo(
    val id: Int,
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    val roleCode: Short,
    val account_deleted: Boolean
) : Principal

/**
 * This class represents an Employee, including their personal information
 * such as login credentials, contact details, and role.
 *
 * @property id Unique identifier for the employee.
 * @property login Login name of the employee.
 * @property password Password for the employee's account.
 * @property name First name of the employee.
 * @property surname Last name of the employee.
 * @property phone Contact phone number of the employee.
 * @property email Email address of the employee.
 * @property roleCode Role code representing the employee's role, which can be one of the following:
 * [Employee.Role.DISPATCHER], [Employee.Role.MANAGER], or [Employee.Role.ADMIN].
 * @property account_deleted Indicates whether the employee's account is deleted.
 *
 * @constructor Creates an Employee instance with the specified details.
 */
@Serializable
data class Employee(
    val id: Int,
    val login: String,
    val password: String,
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    val roleCode: Short,
    val account_deleted: Boolean
) : Principal {
    /**
     * Enum representing role of worker.
     *
     * Contains 3 Roles: [Role.DISPATCHER], [Role.MANAGER], [Role.ADMIN]
     */
    enum class Role(val role: Int) {
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

    /**
     * Property representing the role of the Employee.
     *
     * This property derives its value from the roleCode field by converting it to the corresponding
     * Role instance using the fromInt method.
     */
    val role: Role
        get() = Role.fromInt(roleCode.toInt())


    /**
     * Converts the Employee instance to an EmployeeInfo instance.
     *
     * @return An EmployeeInfo instance containing the employee's details.
     */
    fun toEmployeeInfo(): EmployeeInfo {
        return EmployeeInfo(id, name, surname, phone, email, roleCode, account_deleted)
    }
}

/**
 * Object representing the Employees table in the database.
 *
 * Defines the schema for the Employees table, including fields for the employee's ID, login credentials,
 * personal information, role and account status. The primary key is set to the ID field.
 */
object Employees : Table() {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 20).uniqueIndex()
    val password = varchar("password", 60)
    val name = varchar("name", 40)
    val surname = varchar("surname", 40)
    val phone = varchar("phone", 20).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val role = short("role")
    val account_deleted = bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}