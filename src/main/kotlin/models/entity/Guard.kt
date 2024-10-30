package models.entity

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import models.dto.GuardInfo

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
 * @property email Email address of the guard.
 * @property statusCode Status code indicating the current status of the guard, which can be one of the following:
 * [Guard.GuardStatus.AVAILABLE], [Guard.GuardStatus.UNAVAILABLE], or [Guard.GuardStatus.INTERVENTION].
 * @property location Physical location or assigned area of the guard.
 * @property account_deleted Indicates whether the guard's account has been marked as deleted.
 *
 * @constructor Creates a Guard instance with the specified details, including sensitive credentials.
 */
@Serializable
data class Guard(
    val id: Int,
    val login: String,
    val password: String,
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    var statusCode: Int,
    var location: String,
    val account_deleted: Boolean
) : Principal {

    /**
     * Enum representing status of Guard.
     *
     * Contains 3 states: [GuardStatus.AVAILABLE], [GuardStatus.UNAVAILABLE], [GuardStatus.INTERVENTION], [GuardStatus.NOT_RESPONDING]
     */
    enum class GuardStatus(val status: Int) {
        AVAILABLE(0),
        UNAVAILABLE(1),
        INTERVENTION(2),
        NOT_RESPONDING(3);

        companion object {
            /**
             * Function to convert int to class instance.
             * @param value value of enum
             * @return [Guard.GuardStatus] when number is correct, [Guard.GuardStatus.UNAVAILABLE] in other case
             */
            fun fromInt(value: Int) = entries.firstOrNull { it.status == value } ?: UNAVAILABLE
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

    /**
     * Converts the Guard instance to an GuardInfo instance.
     *
     * @return An GuardInfo instance containing the guard's details.
     */
    fun toGuardInfo(): GuardInfo {
        return GuardInfo(id, name, surname, phone, email, statusCode, location, account_deleted)
    }
}