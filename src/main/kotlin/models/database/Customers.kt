
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Object representing the Customers table in the database.
 *
 * Defines the schema for the Customers table, including fields for the customer's ID, login credentials,
 * personal information, protection_expiration_date and account status. The primary key is set to the ID field.
 */
object Customers : Table() {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 20).uniqueIndex()
    val password = varchar("password", 60)
    val name = varchar("name", 40)
    val surname = varchar("surname", 40)
    val phone = varchar("phone", 20).uniqueIndex()
    val pesel = varchar("pesel", 15).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val protection_expiration_date = datetime("protection_expiration_date").nullable()
    val account_deleted = bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}