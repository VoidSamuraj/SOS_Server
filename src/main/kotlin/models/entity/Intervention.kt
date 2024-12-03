package models.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

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
     * Contains 5 states: [InterventionStatus.CANCELLED_BY_USER], [InterventionStatus.CANCELLED_BY_GUARD], [InterventionStatus.CANCELLED_BY_DISPATCHER], [InterventionStatus.FINISHED], [InterventionStatus.IN_PROGRESS], [InterventionStatus.CONFIRMED]
     */
    enum class InterventionStatus(val status: Int) {
        CANCELLED_BY_USER(0),
        CANCELLED_BY_GUARD(1),
        CANCELLED_BY_DISPATCHER(2),
        FINISHED(3),
        IN_PROGRESS(4),
        CONFIRMED(5);

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