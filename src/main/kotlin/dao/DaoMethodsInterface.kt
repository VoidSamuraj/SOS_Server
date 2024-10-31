package dao

import kotlinx.datetime.LocalDateTime
import models.dto.CustomerInfo
import models.dto.EmployeeInfo
import models.dto.GuardInfo
import models.entity.Customer
import models.entity.Employee
import models.entity.Guard
import models.entity.Intervention
import models.entity.Report
import org.jetbrains.exposed.sql.Column

/**
 * DaoMethodsInterface defines the methods for CRUD operations and additional functionalities
 * related to Customers, Interventions, Reports, Guards, and Employees in the system.
 * Each method is designed to interact asynchronously with the database.
 */
interface DaoMethodsInterface {

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Customer Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Checks if there exists customer with provided login
     *
     * @param login The customer's login.
     * @return A Boolean: true if the customer with provided login was found.
     */
    suspend fun isCustomerLoginUsed(login: String): Boolean


    /**
     * Adds a new customer to the database.
     *
     * @param login The customer's login.
     * @param password The customer's password.
     * @param name The customer's name.
     * @param surname The customer's surname.
     * @param phone The customer's phone number.
     * @param pesel The customer's national identification number (PESEL).
     * @param email The customer's email address.
     * @param protectionExpirationDate The optional date when the customer's protection expires.
     *
     * @return A Triple containing:
     *         - Boolean: Success status (true if the customer was successfully added).
     *         - String: A message providing additional context (e.g., error or success message).
     *         - Customer?: The newly added Customer object (if successful), or null if unsuccessful.
     */
    suspend fun addCustomer(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        pesel: String,
        email: String,
        protectionExpirationDate: LocalDateTime? = null
    ): Triple<Boolean, String, Customer?>

    /**
     * Updates customer details, including login and password. Verifies if provided password is equal to the password associated with id in database.
     *
     * @param id The ID of the customer to update.
     * @param login The customer's new login (optional).
     * @param password The customer's current password (mandatory).
     * @param newPassword The customer's new password (optional).
     * @param name The customer's new name (optional).
     * @param surname The customer's new surname (optional).
     * @param phone The customer's new phone number (optional).
     * @param pesel The customer's new PESEL (optional).
     * @param email The customer's new email (optional).
     * @param protectionExpirationDate The new protection expiration date (optional).
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the customer was successfully updated).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun editCustomer(
        id: Int,
        login: String? = null,
        password: String,
        newPassword: String? = null,
        name: String? = null,
        surname: String? = null,
        phone: String? = null,
        pesel: String? = null,
        email: String? = null,
        protectionExpirationDate: LocalDateTime? = null
    ): Pair<String, Customer?>

    /**
     * Updates basic customer details without modifying login or password. Verifies if id exists in database.
     *
     * @param id The ID of the customer to update.
     * @param name The customer's new name (optional).
     * @param surname The customer's new surname (optional).
     * @param phone The customer's new phone number (optional).
     * @param pesel The customer's new PESEL (optional).
     * @param email The customer's new email (optional).
     * @param isActive The active status of the customer (optional).
     * @param protectionExpirationDate The new protection expiration date (optional).
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the customer was successfully updated).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun editCustomer(
        id: Int,
        name: String? = null,
        surname: String? = null,
        phone: String? = null,
        pesel: String? = null,
        email: String? = null,
        isActive: Boolean? = null,
        protectionExpirationDate: LocalDateTime? = null
    ): Pair<Boolean, String>

    /**
     * Deletes (deactivates) a customer by their ID.
     *
     * @param id The ID of the customer to delete.
     *
     * @return Boolean indicating success (true if the customer was successfully deleted).
     */
    suspend fun deleteCustomer(id: Int): Boolean

    /**
     * Restores (reactivates) a deleted customer by their ID.
     *
     * @param id The ID of the customer to restore.
     *
     * @return Boolean indicating success (true if the customer was successfully restored).
     */
    suspend fun restoreCustomer(id: Int): Boolean

    /**
     * Fetches a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     *
     * @return The Customer object if found, or null if not found.
     */
    suspend fun getCustomer(id: Int): Customer?

    /**
     * Fetches a customer by their login and password.
     *
     * @param login The customer's login.
     * @param password The customer's password.
     *
     * @return A Pair containing:
     *         - String: A message providing additional context (e.g., error or success message).
     *         - Customer?: The retrieved Customer object, or null if not found.
     */
    suspend fun getCustomer(login: String, password: String): Pair<String, Customer?>

    /**
     * Fetches an customer by their email address.
     *
     * @param email The customer's email address.
     *
     * @return The Customer object if found, or null if not found.
     */
    suspend fun getCustomer(email: String): Customer?

    /**
     * Retrieves a paginated list of customers with optional filtering and sorting.
     *
     * @param page The page number to retrieve, starts from 0.
     * @param pageSize The number of customers per page.
     * @param filterColumn The optional column to filter by.
     * @param filterValue The optional value to filter by.
     * @param filterType The optional filter type (e.g., "equals", "like").
     * @param sortBy The optional column to sort by.
     * @param sortDir The optional sort direction ("asc" for ascending, "desc" for descending).
     *
     * @return A list of CustomerInfo objects matching the criteria.
     */
    suspend fun getCustomers(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>? = null,
        filterValue: String? = null,
        filterType: String? = null,
        sortBy: Column<out Any>? = null,
        sortDir: String? = "asc"
    ): List<CustomerInfo>

    /**
     * Retrieves a list of selected customers.
     *
     * @param ids ids of selected customers.
     *
     * @return A list of CustomerInfo objects matching the criteria.
     */
    suspend fun getCustomers(ids: List<Int>): List<CustomerInfo>

    /**
     * Retrieves a list of all customers.
     *
     * @return A list of all CustomerInfo objects.
     */
    suspend fun getAllCustomers(): List<CustomerInfo>


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Intervention Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds a new intervention to the database.
     *
     * @param reportId The ID of the related report.
     * @param guardId The ID of the guard assigned to the intervention.
     * @param employeeId The ID of the employee managing the intervention.
     * @param startTime The start time of the intervention.
     * @param endTime The end time of the intervention.
     * @param status The current status of the intervention.
     *
     * @return Boolean indicating success (true if the intervention was successfully added).
     */
    suspend fun addIntervention(
        reportId: Int,
        guardId: Int,
        employeeId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        status: Intervention.InterventionStatus
    ): Boolean

    /**
     * Updates the details of an intervention record with the specified report ID.
     *
     * @param reportId The unique identifier of the report associated with the intervention to be updated.
     * @param startTime Optional parameter for the intervention's start time. If provided, it will update the record.
     * @param endTime Optional parameter for the intervention's end time. If provided, it will update the record.
     * @param status Optional status update for the intervention, indicating the current state (e.g., IN_PROGRESS, COMPLETED).
     *
     * @return `true` if the update was successful; `false` otherwise (e.g., if the record does not exist or cannot be updated).
     */
    suspend fun editIntervention(reportId:Int, startTime: LocalDateTime?, endTime: LocalDateTime?, status: Intervention.InterventionStatus?): Boolean

    /**
     * Retrieves an intervention by its ID.
     *
     * @param id The ID of the intervention to retrieve.
     *
     * @return The Intervention object if found, or null if not found.
     */
    suspend fun getIntervention(id: Int): Intervention?

    /**
     * Retrieves an intervention by report's ID.
     *
     * @param reportId The ID of the report associated with intervention to retrieve.
     *
     * @return The Intervention object if found, or null if not found.
     */
    suspend fun getInterventionByReport(reportId: Int): Intervention?

    /**
     * Check if guard with provided is assigned to active intervention
     *
     * @param guardId The ID of the guard.
     *
     * @return String containing reportId and location {"reportId": , "lat": , "lng": } or null if there is no active intervention for the guard.
     */
    suspend fun isActiveInterventionAssignedToGuard(guardId:Int): String?

    /**
     * Retrieves a paginated list of interventions with optional filtering and sorting.
     *
     * @param page The page number to retrieve, starts from 0.
     * @param pageSize The number of interventions per page.
     * @param filterColumn The optional column to filter by.
     * @param filterValue The optional value to filter by.
     * @param filterType The optional filter type (e.g., "equals", "like").
     * @param sortBy The optional column to sort by.
     * @param sortDir The optional sort direction ("asc" for ascending, "desc" for descending).
     *
     * @return A list of Intervention objects matching the criteria.
     */
    suspend fun getInterventions(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>? = null,
        filterValue: String? = null,
        filterType: String? = null,
        sortBy: Column<out Any>? = null,
        sortDir: String? = "asc"
    ): List<Intervention>

    /**
     * Retrieves a list of all interventions.
     *
     * @return A list of all Intervention objects.
     */
    suspend fun getAllInterventions(): List<Intervention>


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Report Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds a new report to the database.
     *
     * @param clientId The ID of the client associated with the report.
     * @param location The location of the report.
     * @param date The date and time when the report was created.
     * @param status The current status of the report.
     *
     * @return Int representing id (-1 if failure).
     */
    suspend fun addReport(clientId: Int, location: String, date: LocalDateTime, status: Report.ReportStatus): Int

    /**
     * Deletes a report by their ID
     *
     * @param id The ID of the report to delete.
     *
     * @return Boolean indicating success (true if the report was successfully deleted).
     */
    suspend fun deleteReport(id: Int): Boolean

    /**
     * Updates the location of a report.
     *
     * @param id The ID of the report to update.
     * @param location The new location of the report.
     *
     * @return Boolean indicating success (true if the location was successfully updated).
     */
    suspend fun updateReportLocation(id: Int, location: String): Boolean

    /**
     * Changes the status of a report.
     *
     * @param id The ID of the report to update.
     * @param status The new status of the report.
     *
     * @return Report indicating success, null in other cases.
     */
    suspend fun changeReportStatus(id: Int, status: Report.ReportStatus): Report?

    /**
     * Fetches a report by its ID.
     *
     * @param id The ID of the report to retrieve.
     *
     * @return The Report object if found, or null if not found.
     */
    suspend fun getReport(id: Int): Report?

    /**
     * Retrieves a paginated list of reports with optional filtering and sorting.
     *
     * @param page The page number to retrieve starting from 0.
     * @param pageSize The number of reports per page.
     * @param filterColumn The optional column to filter by.
     * @param filterValue The optional value to filter by.
     * @param filterType The optional filter type (e.g., "equals", "like").
     * @param sortBy The optional column to sort by.
     * @param sortDir The optional sort direction ("asc" for ascending, "desc" for descending).
     *
     * @return A list of Report objects matching the criteria.
     */
    suspend fun getReports(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>? = null,
        filterValue: String? = null,
        filterType: String? = null,
        sortBy: Column<out Any>? = null,
        sortDir: String? = "asc"
    ): List<Report>

    /**
     * Retrieves a list of all reports.
     *
     * @param filterFinished A boolean flag to indicate whether to filter reports based on their finished status.
     *                       - `true`: Returns only not finished reports.
     *                       - `false`: Returns all reports
     * @return A list of reports (`List<Report>`), filtered according to the `filterFinished` parameter.
     * @throws Exception If there is an issue retrieving the reports from the data source.
     */
    suspend fun getAllReports(filterFinished: Boolean): List<Report>


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Guard Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks if there exists guard with provided login
     *
     * @param login The guard's login.
     * @return A Boolean: true if the guard with provided login was found.
     */
    suspend fun isGuardLoginUsed(login: String): Boolean

    /**
     * Adds a new guard to the database.
     *
     * @param login The guard's login.
     * @param password The guard's password.
     * @param name The guard's first name.
     * @param surname The guard's surname.
     * @param phone The guard's phone number.
     * @param email Email address of the guard.
     *
     * @return A Triple containing:
     *         - Boolean: Success status (true if the guard was successfully added).
     *         - String: A message providing additional context (e.g., error or success message).
     *         - Guard?: The newly added Guard object (if successful), or null if unsuccessful.
     */
    suspend fun addGuard(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        email: String
    ): Triple<Boolean, String, Guard?>

    /**
     * Updates guard details, including login and password. Verifies if provided password is equal to the password associated with id in database.
     *
     * @param id The ID of the guard to update.
     * @param login The guard's new login (optional).
     * @param password The guard's current password (mandatory).
     * @param newPassword The guard's new password (optional).
     * @param name The guard's first name (optional).
     * @param surname The guard's surname (optional).
     * @param phone The guard's phone number (optional).
     * @param email Email address of the guard (optional).
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the guard was successfully updated).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun editGuard(
        id: Int,
        login: String? = null,
        password: String,
        newPassword: String? = null,
        name: String? = null,
        surname: String? = null,
        phone: String? = null,
        email: String? = null
    ): Pair<String, Guard?>

    /**
     * Updates basic guard details without modifying login or password. Verifies if id exists in database.
     *
     * @param id The ID of the guard to update.
     * @param name The guard's first name (optional).
     * @param surname The guard's surname (optional).
     * @param phone The guard's phone number (optional).
     * @param email Email address of the guard (optional).
     * @param isActive The active status of the guard (optional).
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the guard was successfully updated).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun editGuard(
        id: Int,
        name: String? = null,
        surname: String? = null,
        phone: String? = null,
        email: String? = null,
        isActive: Boolean? = null
    ): Pair<Boolean, String>


    /**
     * Changes the password of an guard.
     *
     * @param id The ID of the guard whose password will be changed.
     * @param password The new password to assign.
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the guard's password was successfully changed).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun changeGuardPassword(id: Int, password: String): Pair<Boolean, String>

    /**
     * Deletes (deactivates) a guard by their ID.
     *
     * @param id The ID of the guard to delete.
     *
     * @return Boolean indicating success (true if the guard was successfully deleted).
     */
    suspend fun deleteGuard(id: Int): Boolean

    /**
     * Restores (reactivates) a deleted guard by their ID.
     *
     * @param id The ID of the guard to restore.
     *
     * @return Boolean indicating success (true if the guard was successfully restored).
     */
    suspend fun restoreGuard(id: Int): Boolean

    /**
     * Fetches a guard by their ID.
     *
     * @param id The ID of the guard to retrieve.
     *
     * @return The Guard object if found, or null if not found.
     */
    suspend fun getGuard(id: Int): Guard?

    /**
     * Fetches an guard by their email address.
     *
     * @param email The guard's email address.
     *
     * @return The Guard object if found, or null if not found.
     */
    suspend fun getGuard(email: String): Guard?

    /**
     * Fetches a guard by their login and password.
     *
     * @param login The guard's login.
     * @param password The guard's password.
     *
     * @return A Pair containing:
     *         - String: A message providing additional context (e.g., error or success message).
     *         - Guard?: The retrieved Guard object, or null if not found.
     */
    suspend fun getGuard(login: String, password: String): Pair<String, Guard?>

    /**
     * Retrieves a paginated list of guards with optional filtering and sorting.
     *
     * @param page The page number to retrieve starts from 0.
     * @param pageSize The number of guards per page.
     * @param filterColumn The optional column to filter by.
     * @param filterValue The optional value to filter by.
     * @param filterType The optional filter type (e.g., "equals", "like").
     * @param sortBy The optional column to sort by.
     * @param sortDir The optional sort direction ("asc" for ascending, "desc" for descending).
     *
     * @return A list of GuardInfo objects matching the criteria.
     */
    suspend fun getGuards(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>? = null,
        filterValue: String? = null,
        filterType: String? = null,
        sortBy: Column<out Any>? = null,
        sortDir: String? = "asc"
    ): List<GuardInfo>

    /**
     * Retrieves a list of selected guards.
     *
     * @param ids ids of selected guards.
     *
     * @return A list of GuardInfo objects matching the criteria.
     */
    suspend fun getGuards(ids: List<Int>): List<GuardInfo>

    /**
     * Retrieves a list of all guards.
     *
     * @return A list of all GuardInfo objects.
     */
    suspend fun getAllGuards(): List<GuardInfo>


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Employee Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Deletes (deactivates) an employee by their ID.
     *
     * @param id The ID of the employee to delete.
     *
     * @return Boolean indicating success (true if the employee was successfully deleted).
     */
    suspend fun deleteEmployee(id: Int): Boolean

    /**
     * Restores (reactivates) a deleted employee by their ID.
     *
     * @param id The ID of the employee to restore.
     *
     * @return Boolean indicating success (true if the employee was successfully restored).
     */
    suspend fun restoreEmployee(id: Int): Boolean

    /**
     * Updates employee details, including login and password. Verifies if provided password is equal to the password associated with id in database.
     *
     * @param id The ID of the employee to update.
     * @param login The employee's new login (optional).
     * @param password The employee's current password (mandatory).
     * @param newPassword The employee's new password (optional).
     * @param name The employee's first name (optional).
     * @param surname The employee's surname (optional).
     * @param phone The employee's phone number (optional).
     * @param email The employee's email address (optional).
     * @param role The employee's new role (optional).
     *
     * @return A Triple containing:
     *         - Boolean: Success status (true if the employee was successfully updated).
     *         - String: A message providing additional context (e.g., error or success message).
     *         - EmployeeInfo?: An edited object or null.
     */
    suspend fun editEmployee(
        id: Int,
        login: String? = null,
        password: String,
        newPassword: String? = null,
        name: String? = null,
        surname: String? = null,
        phone: String? = null,
        email: String? = null,
        role: Employee.Role? = null
    ): Triple<Boolean, String, EmployeeInfo?>

    /**
     * Updates basic employee details without modifying login or password. Verifies if id exists in database.
     *
     * @param id The ID of the employee to update.
     * @param name The employee's first name (optional).
     * @param surname The employee's surname (optional).
     * @param phone The employee's phone number (optional).
     * @param email The employee's email address (optional).
     * @param role The employee's new role (optional).
     * @param isActive The active status of the employee (optional).
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the employee was successfully updated).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun editEmployee(
        id: Int,
        name: String? = null,
        surname: String? = null,
        phone: String? = null,
        email: String? = null,
        role: Employee.Role? = null,
        isActive: Boolean? = null
    ): Pair<Boolean, String>

    /**
     * Changes the role of an employee.
     *
     * @param id The ID of the employee whose role will be changed.
     * @param role The new role to assign to the employee.
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the employee's role was successfully changed).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun changeEmployeeRole(id: Int, role: Employee.Role): Pair<Boolean, String>

    /**
     * Changes the password of an employee.
     *
     * @param id The ID of the employee whose password will be changed.
     * @param password The new password to assign.
     *
     * @return A Pair containing:
     *         - Boolean: Success status (true if the employee's password was successfully changed).
     *         - String: A message providing additional context (e.g., error or success message).
     */
    suspend fun changeEmployeePassword(id: Int, password: String): Pair<Boolean, String>

    /**
     * Adds a new employee to the database.
     *
     * @param login The employee's login.
     * @param password The employee's password.
     * @param name The employee's first name.
     * @param surname The employee's surname.
     * @param phone The employee's phone number.
     * @param email The employee's email address.
     * @param role The employee's role.
     *
     * @return A Triple containing:
     *         - Boolean: Success status (true if the employee was successfully added).
     *         - String: A message providing additional context (e.g., error or success message).
     *         - Employee?: The newly added Employee object (if successful), or null if unsuccessful.
     */
    suspend fun addEmployee(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        email: String,
        role: Employee.Role
    ): Triple<Boolean, String, Employee?>

    /**
     * Fetches an employee by their ID.
     *
     * @param id The ID of the employee to retrieve.
     *
     * @return The Employee object if found, or null if not found.
     */
    suspend fun getEmployee(id: Int): Employee?

    /**
     * Fetches an employee by their email address.
     *
     * @param email The employee's email address.
     *
     * @return The Employee object if found, or null if not found.
     */
    suspend fun getEmployee(email: String): Employee?

    /**
     * Fetches an employee by their login and password.
     *
     * @param login The employee's login.
     * @param password The employee's password.
     *
     * @return A Pair containing:
     *         - String: A message providing additional context (e.g., error or success message).
     *         - Employee?: The retrieved Employee object, or null if not found.
     */
    suspend fun getEmployee(login: String, password: String): Pair<String, Employee?>

    /**
     * Retrieves a paginated list of employees with optional filtering and sorting.
     *
     * @param page The page number to retrieve starts from 0.
     * @param pageSize The number of employees per page.
     * @param filterColumn The optional column to filter by.
     * @param filterValue The optional value to filter by.
     * @param filterType The optional filter type (e.g., "equals", "like").
     * @param sortBy The optional column to sort by.
     * @param sortDir The optional sort direction ("asc" for ascending, "desc" for descending).
     *
     * @return A list of EmployeeInfo objects matching the criteria.
     */
    suspend fun getEmployees(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>? = null,
        filterValue: String? = null,
        filterType: String? = null,
        sortBy: Column<out Any>? = null,
        sortDir: String? = "asc"
    ): List<EmployeeInfo>

    /**
     * Retrieves a list of selected employees.
     *
     * @param ids ids of selected employees.
     *
     * @return A list of EmployeeInfo objects matching the criteria.
     */
    suspend fun getEmployees(ids: List<Int>): List<EmployeeInfo>

    /**
     * Retrieves a list of all employees.
     *
     * @return A list of all EmployeeInfo objects.
     */
    suspend fun getAllEmployees(): List<EmployeeInfo>
}