package models.entity

import io.ktor.server.auth.Principal
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import models.dto.CustomerInfo


/**
 * Data class representing a Customer.
 *
 * This class contains comprehensive information about a customer, including
 * their personal details, login credentials, and account status.
 *
 * @property id Unique identifier for the customer.
 * @property login Login name of the customer.
 * @property password Password for the customer's account.
 * @property phone Contact phone number of the customer.
 * @property pesel Personal Identification Number of the customer.
 * @property email Email address of the customer.
 * @property account_deleted Indicates whether the customer's account is deleted.
 * @property protection_expiration_date Optional expiration date for protection services.
 *
 * @constructor Creates a Customer instance with the specified details.
 */
@Serializable
data class Customer(
    val id: Int,
    val login: String,
    val password: String,
    val name: String,
    val surname: String,
    val phone: String,
    val pesel: String,
    val email: String,
    val account_deleted: Boolean,
    val protection_expiration_date: LocalDateTime? = null
) : Principal {
    /**
     * Converts the Customer instance to an CustomerInfo instance.
     *
     * @return An CustomerInfo instance containing the customer's details.
     */
    fun toCustomerInfo(): CustomerInfo {
        return CustomerInfo(id, name, surname, phone, pesel, email, account_deleted, protection_expiration_date)
    }
}