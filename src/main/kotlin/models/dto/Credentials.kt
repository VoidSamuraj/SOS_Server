package models.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing user credentials used for authentication.
 *
 * @property login The user's login, typically an email or username.
 * @property password The user's password used for authentication.
 *
 * This class is annotated with `@Serializable` to allow easy conversion to and from JSON format,
 * making it suitable for data transfer in APIs.
 */
@Serializable
data class Credentials(
    val login: String,
    val password: String
)