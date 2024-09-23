import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

@Serializable
data class Intervention(val id:Int,val report_id: Int,val guard_id:Int, val employee_id:Int, val start_time:LocalDateTime, val end_time:LocalDateTime, val statusCode:Short){
    /**
     * Enum representing status of Intervention.
     *
     * Contains 2 states: [InterventionStatus.CANCELLED_BY_USER], [InterventionStatus.CANCELLED_BY_GUARD], [InterventionStatus.FINISHED]
     */
    enum class InterventionStatus(val status:Int) {
        CANCELLED_BY_USER(0),
        CANCELLED_BY_GUARD(1),
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

    val status: InterventionStatus
        get() = InterventionStatus.fromInt(statusCode.toInt())
}
object Interventions : Table() {
    val id = integer("id").autoIncrement()
    val report_id = reference("report_id", Reports.id, onDelete = ReferenceOption.CASCADE )
    val guard_id = reference("guard_id", Guards.id, onDelete = ReferenceOption.NO_ACTION)
    val employee_id = reference("dispatcher_id", Employees.id, onDelete = ReferenceOption.NO_ACTION)
    val start_time = datetime("start_time")
    val end_time = datetime("end_time")
    val status = short("status")
    override val primaryKey = PrimaryKey(id)
}