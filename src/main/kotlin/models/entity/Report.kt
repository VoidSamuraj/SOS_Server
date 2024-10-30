package models.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Data class representing a Report.
 *
 * This class encapsulates all necessary details about a report,
 * including the ID of the associated client, the location of the incident,
 * and the date and time when the report was created.
 *
 * @property id Unique identifier for the report.
 * @property client_id Identifier of the client associated with the report.
 * @property location The geographical location where the incident occurred.
 * @property date The date and time when the report was generated.
 * @property statusCode Status code representing the current state of the report, which can be one of the following:
 * [Report.ReportStatus.WAITING], [Report.ReportStatus.IN_PROGRESS] or [Report.ReportStatus.FINISHED]
 *
 * @constructor Creates a Report instance with the specified details.
 */
@Serializable
data class Report(
    val id: Int,
    val client_id: Int,
    val location: String,
    val date: LocalDateTime,
    private val statusCode: Short
) {

    /**
     * Enum representing status of Report.
     *
     * Contains 3 states: [ReportStatus.WAITING], [ReportStatus.IN_PROGRESS], [ReportStatus.FINISHED]
     */
    enum class ReportStatus(val status: Int) {
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

    /**
     * Property representing the status of the Report.
     *
     * This property derives its value from the statusCode field by converting it to the corresponding
     * ReportStatus instance using the fromInt method.
     */
    val status: ReportStatus
        get() = ReportStatus.fromInt(statusCode.toInt())
}