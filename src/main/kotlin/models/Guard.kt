import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Guard(val id:Int,val name: String, val surname: String,val phone: String, val statusCode:Int, val location:String): Principal{

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
    val status: GuardStatus
    get() = GuardStatus.fromInt(statusCode)
}
object Guards : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name",40)
    val surname = varchar("surname", 40)
    val phone =  varchar("phone", 20).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}