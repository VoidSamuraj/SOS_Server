
import org.jetbrains.exposed.sql.Table


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