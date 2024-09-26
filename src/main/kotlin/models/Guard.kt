import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * Data class representing a simplified version of a Guard.
 *
 * This class excludes sensitive data related to credentials and focuses on the personal information
 * necessary for identifying and managing the guard within the system.
 *
 * @property id Unique identifier for the guard.
 * @property name First name of the guard.
 * @property surname Last name of the guard.
 * @property phone Contact phone number of the guard.
 * @property statusCode Status code indicating the current status of the guard, which can be one of the following:
 * [Guard.GuardStatus.AVAILABLE], [Guard.GuardStatus.UNAVAILABLE], or [Guard.GuardStatus.INTERVENTION].
 * @property location Physical location or assigned area of the guard.
 * @property account_deleted Indicates whether the guard's account has been marked as deleted.
 *
 * @constructor Creates a GuardInfo instance with the specified details.
 */
@Serializable
data class GuardInfo(val id:Int, val name: String, val surname: String,val phone: String, val statusCode:Int, val location:String, val account_deleted:Boolean): Principal

/**
 * Data class representing a Guard.
 *
 * This class includes all necessary details about a guard, including their login credentials,
 * personal information, and status within the system.
 *
 * @property id Unique identifier for the guard.
 * @property login The login name for the guard's account, used for authentication.
 * @property password The password for the guard's account, which should be securely handled.
 * @property name First name of the guard.
 * @property surname Last name of the guard.
 * @property phone Contact phone number of the guard.
 * @property statusCode Status code indicating the current status of the guard, which can be one of the following:
 * [Guard.GuardStatus.AVAILABLE], [Guard.GuardStatus.UNAVAILABLE], or [Guard.GuardStatus.INTERVENTION].
 * @property location Physical location or assigned area of the guard.
 * @property account_deleted Indicates whether the guard's account has been marked as deleted.
 *
 * @constructor Creates a Guard instance with the specified details, including sensitive credentials.
 */
@Serializable
data class Guard(val id:Int, val login: String,val password:String, val name: String, val surname: String,val phone: String, val statusCode:Int, val location:String, val account_deleted:Boolean): Principal{

    /**
     * Enum representing status of Guard.
     *
     * Contains 3 states: [GuardStatus.AVAILABLE], [GuardStatus.UNAVAILABLE], [GuardStatus.INTERVENTION]
     */
    enum class GuardStatus(val status: Int) {
        AVAILABLE(0),
        UNAVAILABLE(1),
        INTERVENTION(2);

        companion object {
            /**
             * Function to convert int to class instance.
             * @param value value of enum
             * @throws IllegalArgumentException if enum not contains object with such number
             */
            fun fromInt(value: Int) = entries.firstOrNull { it.status == value }
                ?: throw IllegalArgumentException("Unknown ReportStatus for value: $value")
        }
    }

    /**
     * Property representing the status of the Guard.
     *
     * This property derives its value from the statusCode field by converting it to the corresponding
     * GuardStatus instance using the fromInt method.
     */
    val status: GuardStatus
        get() = GuardStatus.fromInt(statusCode)
}

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
    val name = varchar("name",40)
    val surname = varchar("surname", 40)
    val phone =  varchar("phone", 20).uniqueIndex()
    val account_deleted=bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}