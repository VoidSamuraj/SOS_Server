package models.dto

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

/**
 * Data class representing a reduced Customer.
 *
 * This class includes essential information about a customer, excluding sensitive
 * data such as login credentials and password.
 *
 * @property id Unique identifier for the customer.
 * @property phone Contact phone number of the customer.
 * @property pesel Personal Identification Number of the customer.
 * @property email Email address of the customer.
 * @property account_deleted Indicates whether the customer's account is deleted.
 * @property protection_expiration_date Optional expiration date for protection services.
 * @property token Optional token string, provided for  CRUD requests.
 *
 * @constructor Creates a CustomerInfo instance with the specified details.
 */
@Serializable
data class CustomerInfo(
    val id: Int,
    val name: String,
    val surname: String,
    val phone: String,
    val pesel: String,
    val email: String,
    val account_deleted: Boolean,
    val protection_expiration_date: String? = null,
    var token: String? = null
) : Principal