import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


/**
 * Data class representing an Intervention.
 *
 * This class encapsulates all necessary details about an intervention,
 * including the IDs of the associated report and guard, along with the start and end times.
 *
 * @property id Unique identifier for the intervention.
 * @property report_id Identifier of the associated report that triggered the intervention.
 * @property guard_id Identifier of the guard involved in the intervention.
 * @property employee_id Identifier of the employee (e.g., dispatcher) who initiated or is overseeing the intervention.
 * @property start_time The date and time when the intervention began.
 * @property end_time The date and time when the intervention concluded.
 * @property statusCode Status code indicating the current status of the intervention, which can be one of the following:
 * [Intervention.InterventionStatus.CANCELLED_BY_USER], [Intervention.InterventionStatus.CANCELLED_BY_GUARD] or [Intervention.InterventionStatus.FINISHED]
 *
 * @constructor Creates an Intervention instance with the specified details.
 */
@Serializable
data class Intervention(
    val id: Int,
    val report_id: Int,
    val guard_id: Int,
    val employee_id: Int,
    val start_time: LocalDateTime,
    val end_time: LocalDateTime,
    val statusCode: Short
) {
    /**
     * Enum representing status of Intervention.
     *
     * Contains 3 states: [InterventionStatus.CANCELLED_BY_USER], [InterventionStatus.CANCELLED_BY_GUARD], [InterventionStatus.FINISHED]
     */
    enum class InterventionStatus(val status: Int) {
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

    /**
     * Property representing the status of the Intervention.
     *
     * This property derives its value from the statusCode field by converting it to the corresponding
     * InterventionStatus instance using the fromInt method.
     */
    val status: InterventionStatus
        get() = InterventionStatus.fromInt(statusCode.toInt())
}

/**
 * Object representing the Interventions table in the database.
 *
 * Defines the schema for the Interventions table, including fields for the intervention ID, report ID,
 * guard ID, employee ID, start time, end time, and status code. The primary key is set to the ID field.
 */
object Interventions : Table() {
    val id = integer("id").autoIncrement()
    val report_id = reference("report_id", Reports.id, onDelete = ReferenceOption.CASCADE)
    val guard_id = reference("guard_id", Guards.id, onDelete = ReferenceOption.NO_ACTION)
    val employee_id = reference("dispatcher_id", Employees.id, onDelete = ReferenceOption.NO_ACTION)
    val start_time = datetime("start_time")
    val end_time = datetime("end_time")
    val status = short("status")
    override val primaryKey = PrimaryKey(id)
}