
import org.jetbrains.exposed.sql.Table

/**
 * Object representing the Guards table in the database.
 *
 * Defines the schema for the Guards table, including fields for the guard's ID, login credentials,
 * personal information, and account status. The primary key is set to the ID field.
 */
object Guards : Table() {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 20).uniqueIndex()
    val password = varchar("password", 60)
    val name = varchar("name", 40)
    val surname = varchar("surname", 40)
    val phone = varchar("phone", 20).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val account_deleted = bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}