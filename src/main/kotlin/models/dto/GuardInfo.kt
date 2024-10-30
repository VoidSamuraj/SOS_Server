package models.dto

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

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
 * @property email Email address of the guard.
 * @property statusCode Status code indicating the current status of the guard, which can be one of the following:
 * [models.entity.Guard.GuardStatus.AVAILABLE], [models.entity.Guard.GuardStatus.UNAVAILABLE],
 * [models.entity.Guard.GuardStatus.INTERVENTION] or [models.entity.Guard.GuardStatus.NOT_RESPONDING].
 * @property location Physical location or assigned area of the guard.
 * @property account_deleted Indicates whether the guard's account has been marked as deleted.
 * @property token Optional token string, provided for  CRUD requests.
 *
 * @constructor Creates a GuardInfo instance with the specified details.
 */
@Serializable
data class GuardInfo(
    val id: Int,
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    var statusCode: Int,
    var location: String,
    val account_deleted: Boolean,
    var token: String? = null
) : Principal