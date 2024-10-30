package models.entity

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import models.dto.EmployeeInfo

/**
 * This class represents an Employee, including their personal information
 * such as login credentials, contact details, and role.
 *
 * @property id Unique identifier for the employee.
 * @property login Login name of the employee.
 * @property password Password for the employee's account.
 * @property name First name of the employee.
 * @property surname Last name of the employee.
 * @property phone Contact phone number of the employee.
 * @property email Email address of the employee.
 * @property roleCode Role code representing the employee's role, which can be one of the following:
 * [Employee.Role.DISPATCHER], [Employee.Role.MANAGER], or [Employee.Role.ADMIN].
 * @property account_deleted Indicates whether the employee's account is deleted.
 *
 * @constructor Creates an Employee instance with the specified details.
 */
@Serializable
data class Employee(
    val id: Int,
    val login: String,
    val password: String,
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    val roleCode: Short,
    val account_deleted: Boolean
) : Principal {
    /**
     * Enum representing role of worker.
     *
     * Contains 3 Roles: [Role.DISPATCHER], [Role.MANAGER], [Role.ADMIN]
     */
    enum class Role(val role: Int) {
        DISPATCHER(0),
        MANAGER(1),
        ADMIN(2);

        companion object {
            /**
             * Convert int to class instance.
             *
             * @param value value of enum
             * @throws IllegalArgumentException if enum not contains object with such number
             */
            fun fromInt(value: Int) = entries.firstOrNull { it.role == value }
                ?: throw IllegalArgumentException("Unknown ReportStatus for value: $value")
        }
    }

    /**
     * Property representing the role of the Employee.
     *
     * This property derives its value from the roleCode field by converting it to the corresponding
     * Role instance using the fromInt method.
     */
    val role: Role
        get() = Role.fromInt(roleCode.toInt())


    /**
     * Converts the Employee instance to an EmployeeInfo instance.
     *
     * @return An EmployeeInfo instance containing the employee's details.
     */
    fun toEmployeeInfo(): EmployeeInfo {
        return EmployeeInfo(id, name, surname, phone, email, roleCode, account_deleted)
    }
}