package models.dto

import kotlinx.serialization.Serializable
import io.ktor.server.auth.Principal

/**
 * This class represents a simplified version of an Employee, including their personal information
 * such as contact details and role.
 *
 * @property id Unique identifier for the employee.
 * @property name First name of the employee.
 * @property surname Last name of the employee.
 * @property phone Contact phone number of the employee.
 * @property email Email address of the employee.
 * @property roleCode Role code representing the employee's role, which can be one of the following:
 * [models.entity.Employee.Role.DISPATCHER], [models.entity.Employee.Role.MANAGER], or [models.entity.Employee.Role.ADMIN].
 * @property account_deleted Indicates whether the employee's account has been deleted.
 *
 * @constructor Creates an EmployeeInfo instance with the specified details.
 */

@Serializable
data class EmployeeInfo(
    val id: Int,
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    val roleCode: Short,
    val account_deleted: Boolean
) : Principal
