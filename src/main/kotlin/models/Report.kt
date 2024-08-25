import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

@Serializable
data class Report(val id:Int,val client_id: Int,val location:String, val date:LocalDateTime, private val statusCode:Short){

    /**
     * Enum representing status of Report.
     *
     * Contains 3 states: [ReportStatus.WAITING], [ReportStatus.IN_PROGRESS], [ReportStatus.FINISHED]
     */
    enum class ReportStatus(val status:Int) {
        WAITING(0),
        IN_PROGRESS(1),
        FINISHED(2);

        companion object {
            /**
             * Converts int to class instance.
             *
             * @param value value of enum
             * @throws IllegalArgumentException if enum not contains object with such number
             */
            fun fromInt(value: Int) = entries.firstOrNull { it.status == value }
                ?: throw IllegalArgumentException("Unknown ReportStatus for value: $value")
        }
    }

    val status: ReportStatus
        get() = ReportStatus.fromInt(statusCode.toInt())
}

object Reports : Table() {
    val id = integer("id").autoIncrement()
    val client_id = reference("client_id", SystemClients.id, onDelete = ReferenceOption.NO_ACTION)
    val location = varchar("location", 255)
    val date = datetime("date")
    val status = short("status")
    override val primaryKey = PrimaryKey(id)
}
