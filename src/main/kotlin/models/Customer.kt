import io.ktor.server.auth.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Data class representing a reduced Customer.
 *
 * This class includes essential information about a customer, excluding sensitive
 * data such as login credentials and password.
 *
 * @property id Unique identifier for the customer.
 * @property phone Contact phone number of the customer.
 * @property pesel Personal Identification Number of the customer.
 * @property email Email address of the customer.
 * @property account_deleted Indicates whether the customer's account is deleted.
 * @property protection_expiration_date Optional expiration date for protection services.
 *
 * @constructor Creates a CustomerInfo instance with the specified details.
 */
@Serializable
data class CustomerInfo(val id:Int, val phone:String, val pesel:String, val email:String, val account_deleted:Boolean, val protection_expiration_date: LocalDateTime?=null): Principal

/**
 * Data class representing a Customer.
 *
 * This class contains comprehensive information about a customer, including
 * their personal details, login credentials, and account status.
 *
 * @property id Unique identifier for the customer.
 * @property login Login name of the customer.
 * @property password Password for the customer's account.
 * @property phone Contact phone number of the customer.
 * @property pesel Personal Identification Number of the customer.
 * @property email Email address of the customer.
 * @property account_deleted Indicates whether the customer's account is deleted.
 * @property protection_expiration_date Optional expiration date for protection services.
 *
 * @constructor Creates a Customer instance with the specified details.
 */
@Serializable
data class Customer(val id:Int, val login: String, val password:String, val phone:String, val pesel:String, val email:String, val account_deleted:Boolean, val protection_expiration_date: LocalDateTime?=null): Principal

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
    val phone = varchar("phone", 20).uniqueIndex()
    val pesel = varchar("pesel", 15).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val protection_expiration_date =  datetime("protection_expiration_date").nullable()
    val account_deleted=bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}