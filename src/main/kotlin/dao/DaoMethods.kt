package dao

import Customers
import Employees
import Guards
import Interventions
import Reports
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import models.dto.CustomerInfo
import models.dto.EmployeeInfo
import models.dto.GuardInfo
import models.entity.Customer
import models.entity.Employee
import models.entity.Guard
import models.entity.Intervention
import models.entity.Report
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import plugins.notifyClientsAboutChanges
import security.HashPassword
import security.HashPassword.comparePasswords
import kotlin.collections.mapNotNull

object DaoMethods : DaoMethodsInterface {

    private fun resultRowToClient(row: ResultRow) = Customer(
        id = row[Customers.id],
        login = row[Customers.login],
        password = row[Customers.password],
        name = row[Customers.name],
        surname = row[Customers.surname],
        phone = row[Customers.phone],
        pesel = row[Customers.pesel],
        email = row[Customers.email],
        account_deleted = row[Customers.account_deleted],
        protection_expiration_date = row[Customers.protection_expiration_date]
    )

    private fun resultRowToClientInfo(row: ResultRow) = CustomerInfo(
        id = row[Customers.id],
        name = row[Customers.name],
        surname = row[Customers.surname],
        phone = row[Customers.phone],
        pesel = row[Customers.pesel],
        email = row[Customers.email],
        account_deleted = row[Customers.account_deleted],
        protection_expiration_date = row[Customers.protection_expiration_date].toString()
    )

    private fun resultRowToIntervention(row: ResultRow) = Intervention(
        id = row[Interventions.id],
        report_id = row[Interventions.report_id],
        guard_id = row[Interventions.guard_id],
        employee_id = row[Interventions.employee_id],
        start_time = row[Interventions.start_time],
        end_time = row[Interventions.end_time],
        statusCode = row[Interventions.status],
    )

    private fun resultRowToReport(row: ResultRow) = Report(
        id = row[Reports.id],
        client_id = row[Reports.client_id],
        location = row[Reports.location],
        date = row[Reports.date],
        statusCode = row[Reports.status]
    )

    private fun resultRowToGuard(row: ResultRow) = Guard(
        id = row[Guards.id],
        login = row[Guards.login],
        password = row[Guards.password],
        name = row[Guards.name],
        surname = row[Guards.surname],
        phone = row[Guards.phone],
        email = row[Guards.email],
        statusCode = Guard.GuardStatus.UNAVAILABLE.status,
        location = "",
        account_deleted = row[Guards.account_deleted]
    )

    private fun resultRowToGuardInfo(row: ResultRow) = GuardInfo(
        id = row[Guards.id],
        name = row[Guards.name],
        surname = row[Guards.surname],
        phone = row[Guards.phone],
        email = row[Guards.email],
        statusCode = Guard.GuardStatus.UNAVAILABLE.status,
        location = "",
        account_deleted = row[Guards.account_deleted]
    )

    private fun resultRowToEmployee(row: ResultRow) = Employee(
        id = row[Employees.id],
        login = row[Employees.login],
        password = row[Employees.password],
        name = row[Employees.name],
        surname = row[Employees.surname],
        phone = row[Employees.phone],
        email = row[Employees.email],
        roleCode = row[Employees.role],
        account_deleted = row[Employees.account_deleted]
    )

    private fun resultRowToEmployeeInfo(row: ResultRow) = EmployeeInfo(
        id = row[Employees.id],
        name = row[Employees.name],
        surname = row[Employees.surname],
        phone = row[Employees.phone],
        email = row[Employees.email],
        roleCode = row[Employees.role],
        account_deleted = row[Employees.account_deleted]
    )


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Customer Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun isCustomerLoginUsed(login: String): Boolean {
        return try {
            transaction {
                Customers
                    .selectAll().where { Customers.login eq login }
                    .mapNotNull(::resultRowToClient)
                    .singleOrNull() != null
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            true
        }
    }

    override suspend fun addCustomer(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        pesel: String,
        email: String,
        protectionExpirationDate: LocalDateTime?
    ): Triple<Boolean, String, Customer?> {
        return try {
            transaction {

                val conflict = Customers.selectAll().where {
                    (Customers.login eq login) or
                            (Customers.phone eq phone) or
                            (Customers.pesel eq pesel) or
                            (Customers.email eq email)
                }.firstOrNull()

                if (conflict != null) {
                    val message = when {
                        conflict[Customers.login] == login -> "Login is already taken."
                        conflict[Customers.phone] == phone -> "Phone is already taken."
                        conflict[Customers.pesel] == pesel -> "Pesel is already taken."
                        conflict[Customers.email] == email -> "Email is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction Triple(false, message, null)
                }

                val insertStatement = Customers.insert {
                    it[Customers.login] = login
                    it[Customers.password] = HashPassword.hashPassword(password)
                    it[Customers.name] = name
                    it[Customers.surname] = surname
                    it[Customers.phone] = phone
                    it[Customers.pesel] = pesel
                    it[Customers.email] = email
                    it[Customers.account_deleted] = false
                    it[Customers.protection_expiration_date] = protectionExpirationDate
                }
                if (insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToClient) != null)
                    return@transaction Triple(
                        true,
                        "Customer created successfully.",
                        insertStatement.resultedValues?.firstOrNull()
                            ?.let { resultRow -> resultRowToClient(resultRow) })
                return@transaction Triple(false, "Failed to create client.", null)
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            Triple(false, "An error occurred: ${e.message}", null)
        }
    }

    override suspend fun editCustomer(
        id: Int,
        login: String?,
        password: String,
        newPassword: String?,
        name: String?,
        surname: String?,
        phone: String?,
        pesel: String?,
        email: String?,
        protectionExpirationDate: LocalDateTime?
    ): Pair<String, Customer?> {
        return try {
            transaction {

                val customerExists = Customers.selectAll().where { (Customers.id eq id) }.singleOrNull()
                if (customerExists == null) {
                    return@transaction "Incorrect id for." to null
                }
                if (!comparePasswords(password, resultRowToClient(customerExists).password))
                    return@transaction "Incorrect password." to null

                val conditions = mutableListOf<Op<Boolean>>()
                if (login != null) conditions.add(Customers.login eq login)
                if (pesel != null) conditions.add(Customers.pesel eq pesel)
                if (email != null) conditions.add(Customers.email eq email)

                val conflict = if (conditions.isNotEmpty()) {
                    Customers.selectAll().where {
                        conditions.reduce { acc, condition -> acc or condition } and (Customers.id neq id) // check only for different accounts
                    }.firstOrNull()
                } else {
                    null
                }

                if (conflict != null) {
                    val message = when {
                        login != null && conflict[Customers.login] == login -> "Login is already taken."
                        pesel != null && conflict[Customers.pesel] == pesel -> "Pesel is already taken."
                        email != null && conflict[Customers.email] == email -> "Email is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction message to null
                }

                val result = Customers.update({ Customers.id eq id }) {
                    login?.let { firstName -> it[Customers.login] = firstName }
                    newPassword?.let { newPassword -> it[Customers.password] = newPassword }
                    name?.let { name -> it[Customers.name] = name }
                    surname?.let { surname -> it[Customers.surname] = surname }
                    phone?.let { phone -> it[Customers.phone] = phone }
                    pesel?.let { pesel -> it[Customers.pesel] = pesel }
                    email?.let { email -> it[Customers.email] = email }
                    protectionExpirationDate?.let { protectionExpirationDate ->
                        it[Customers.protection_expiration_date] = protectionExpirationDate
                    }
                } > 0
                if (result) {
                    val customer = Customers.selectAll().where({ Customers.id eq id }).mapNotNull(::resultRowToClient)
                        .singleOrNull()
                    notifyClientsAboutChanges("customers", id)
                    return@transaction "Customer updated successfully." to customer
                }
                return@transaction "Failed to update customer." to null
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            "An error occurred: ${e.message}" to null
        }
    }

    override suspend fun editCustomer(
        id: Int,
        name: String?,
        surname: String?,
        phone: String?,
        pesel: String?,
        email: String?,
        isActive: Boolean?,
        protectionExpirationDate: LocalDateTime?
    ): Pair<Boolean, String> {

        return try{
            transaction {

                val customerExists = Customers.selectAll().where { (Customers.id eq id) }.singleOrNull()
                if (customerExists == null) {
                    return@transaction false to "Incorrect id for."
                }

                val conditions = mutableListOf<Op<Boolean>>()
                if (pesel != null) conditions.add(Customers.pesel eq pesel)
                if (email != null) conditions.add(Customers.email eq email)

                val conflict = if (conditions.isNotEmpty()) {
                    Customers.selectAll().where {
                        conditions.reduce { acc, condition -> acc or condition } and (Customers.id neq id) // check only for different accounts
                    }.firstOrNull()
                } else {
                    null
                }

                if (conflict != null) {
                    val message = when {
                        pesel != null && conflict[Customers.pesel] == pesel -> "Pesel is already taken."
                        email != null && conflict[Customers.email] == email -> "Email is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction Pair(false, message)
                }

                val result = Customers.update({ Customers.id eq id }) {
                    name?.let { name -> it[Customers.name] = name }
                    surname?.let { surname -> it[Customers.surname] = surname }
                    phone?.let { phone -> it[Customers.phone] = phone }
                    pesel?.let { pesel -> it[Customers.pesel] = pesel }
                    email?.let { email -> it[Customers.email] = email }
                    isActive?.let { isActive -> it[Customers.account_deleted] = !isActive }
                    protectionExpirationDate?.let { protectionExpirationDate ->
                        it[Customers.protection_expiration_date] = protectionExpirationDate
                    }
                } > 0
                if (result) {
                    notifyClientsAboutChanges("customers", id)
                    return@transaction true to "Customer updated successfully."
                }
                return@transaction false to "Failed to update customer."
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false to "An error occurred: ${e.message}"
        }
    }

    override suspend fun deleteCustomer(id: Int): Boolean {
        return try{
        transaction {
            val ret = Customers.update({ Customers.id eq id }) {
                it[Customers.account_deleted] = true
            } > 0
            if (ret) {
                notifyClientsAboutChanges("customers", id)
            }
            return@transaction ret
        }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun restoreCustomer(id: Int): Boolean {
        return try{
            transaction {
                val ret = Customers.update({ Customers.id eq id }) {
                    it[Customers.account_deleted] = false
                } > 0
                if (ret)
                    notifyClientsAboutChanges("customers", id)
                return@transaction ret
            }
    } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
        false
    }
    }

    override suspend fun getCustomer(id: Int): Customer? {
        return try {
            transaction {
                Customers
                    .selectAll().where { Customers.id eq id }
                    .mapNotNull(::resultRowToClient)
                    .singleOrNull()
            }
        }catch (e: Exception) {
            System.err.println("Error: ${e.message}")
                null
            }
    }

    override suspend fun getCustomer(email: String): Customer? {
        return try{
            transaction {
                Customers
                    .selectAll().where { Customers.email eq email }
                    .mapNotNull(::resultRowToClient)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getCustomer(login: String, password: String): Pair<String, Customer?> {
        return try{
            transaction {
                val customer = Customers
                    .selectAll().where { (Customers.login eq login) }
                    .mapNotNull(::resultRowToClient)
                    .singleOrNull()
                if (customer == null) {
                    return@transaction "Incorrect login." to null
                }

                if (!comparePasswords(password, customer.password))
                    return@transaction "Incorrect password." to null

                return@transaction "Success" to customer

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            "An error occurred: ${e.message}" to null
        }
    }

    override suspend fun getCustomers(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>?,
        filterValue: String?,
        filterType: String?,
        sortBy: Column<out Any>?,
        sortDir: String?
    ): List<CustomerInfo> {
        return try{
            transaction {
                val offset = (page - 1) * pageSize
                val query = Customers.selectAll().apply {
                    if (filterColumn != null && filterValue != null) {
                        try {
                            if (filterColumn.columnType is StringColumnType) {
                                val column = filterColumn as? Column<String>
                                if (column != null)
                                    when (filterType) {
                                        "contains" -> where { column like "%$filterValue%" }
                                        "startsWith" -> where { column like "$filterValue%" }
                                        "endsWith" -> where { column like "%$filterValue" }
                                    }
                            } else {
                                val column = filterColumn as? Column<Any>
                                if (column != null)
                                    when (filterType) {
                                        "equals" -> where { column eq filterValue }
                                        "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                        "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                        "isAnyOf" -> where { column eq filterValue }
                                    }
                            }
                        } catch (e: Error) {
                            System.err.println("Error: ${e.message}")
                        }

                    }
                }
                if (sortBy != null) {
                    try {
                        query.orderBy(sortBy, if (sortDir == "desc") SortOrder.DESC else SortOrder.ASC)
                    } catch (e: Error) {
                        System.err.println("Error: ${e.message}")
                    }
                }
                query.limit(pageSize, offset.toLong())
                    .map(::resultRowToClientInfo).toList()

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<CustomerInfo>()
        }
    }

    override suspend fun getCustomers(ids: List<Int>): List<CustomerInfo> {
        return try{
            transaction {
            return@transaction Customers
                .selectAll().where { Customers.id inList ids }
                .mapNotNull(::resultRowToClientInfo)

        }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<CustomerInfo>()
        }
    }

    override suspend fun getAllCustomers(): List<CustomerInfo> {
        return try{
            transaction {
            Customers.selectAll()
                .map(::resultRowToClientInfo).toList()
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        emptyList<CustomerInfo>()
    }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Intervention Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun addIntervention(
        reportId: Int,
        guardId: Int,
        employeeId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        status: Intervention.InterventionStatus
    ): Boolean {
        return try{
            transaction {
                val insertStatement = Interventions.insert {
                    it[Interventions.report_id] = reportId
                    it[Interventions.guard_id] = guardId
                    it[Interventions.employee_id] = employeeId
                    it[Interventions.start_time] = startTime
                    it[Interventions.end_time] = endTime
                    it[Interventions.status] = status.status.toShort()
                }
                insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToIntervention) != null
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun editIntervention(
        reportId: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: Intervention.InterventionStatus?,
        filterActive: Boolean
    ): Boolean {
        return try{
            transaction {

                val condition = if (filterActive) {
                    (Interventions.report_id eq reportId) and ((Interventions.status eq Intervention.InterventionStatus.CONFIRMED.status.toShort()) or (Interventions.status eq Intervention.InterventionStatus.IN_PROGRESS.status.toShort()))
                } else {
                    Interventions.report_id eq reportId
                }
                val liczba = Interventions.update({ condition }) {
                    startTime?.let { startTime -> it[Interventions.start_time] = startTime }
                    endTime?.let { endTime -> it[Interventions.end_time] = endTime }
                    status?.let { status -> it[Interventions.status] = status.status.toShort() }
                }
                liczba > 0

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun getIntervention(id: Int): Intervention? {
        return try{
            transaction {
                Interventions
                    .selectAll().where { Interventions.id eq id }
                    .mapNotNull(::resultRowToIntervention)
                    .singleOrNull()
            }
         } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        null
    }
    }

    override suspend fun getActiveInterventionByReport(reportId: Int, filterActive: Boolean): Intervention? {
        return try{
            transaction {
                val condition = if (filterActive) {
                    ((Interventions.status eq Intervention.InterventionStatus.CONFIRMED.status.toShort()) or (Interventions.status eq Intervention.InterventionStatus.IN_PROGRESS.status.toShort())) and (Interventions.report_id eq reportId)
                } else {
                    (Interventions.report_id eq reportId)
                }
                Interventions
                    .selectAll()
                    .where { condition }
                    .mapNotNull(::resultRowToIntervention)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getInterventionByGuard(guardId: Int): Intervention? {
        return try{
            transaction {
                Interventions
                    .selectAll()
                    .where { ((Interventions.status eq Intervention.InterventionStatus.CONFIRMED.status.toShort()) or (Interventions.status eq Intervention.InterventionStatus.IN_PROGRESS.status.toShort())) and (Interventions.guard_id eq guardId) }
                    .mapNotNull(::resultRowToIntervention)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun isActiveInterventionAssignedToGuard(guardId: Int): String? {
        return try{
            transaction {
                val response = (Interventions innerJoin Reports)
                    .select(Reports.location, Reports.id, Interventions.guard_id, Interventions.status)
                    .where {
                        (Interventions.guard_id) eq guardId and
                                ((Interventions.status eq Intervention.InterventionStatus.IN_PROGRESS.status.toShort()) or
                                        (Interventions.status eq Intervention.InterventionStatus.CONFIRMED.status.toShort()))
                    }
                    .mapNotNull {

                        val locationString = it[Reports.location].toString()
                        val id = it[Reports.id]
                        val parser = JsonParser.parseString(locationString).asJsonObject
                        parser.addProperty("reportId", id)
                        parser.toString()
                    }.singleOrNull()
                return@transaction response
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getInterventions(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>?,
        filterValue: String?,
        filterType: String?,
        sortBy: Column<out Any>?,
        sortDir: String?
    ): List<Intervention> {
        return try{
            transaction {
                val offset = (page - 1) * pageSize
                val query = Interventions.selectAll().apply {
                    if (filterColumn != null && filterValue != null) {
                        try {
                            if (filterColumn.columnType is StringColumnType) {
                                val column = filterColumn as? Column<String>
                                if (column != null)
                                    when (filterType) {
                                        "contains" -> where { column like "%$filterValue%" }
                                        "startsWith" -> where { column like "$filterValue%" }
                                        "endsWith" -> where { column like "%$filterValue" }
                                    }
                            } else {
                                val column = filterColumn as? Column<Any>
                                if (column != null)
                                    when (filterType) {
                                        "equals" -> where { column eq filterValue }
                                        "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                        "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                        "isAnyOf" -> where { column eq filterValue }
                                    }
                            }
                        } catch (e: Error) {
                            System.err.println("Error: ${e.message}")
                        }
                    }
                }
                if (sortBy != null) {
                    try {
                        query.orderBy(sortBy, if (sortDir == "desc") SortOrder.DESC else SortOrder.ASC)
                    } catch (e: Error) {
                        System.err.println("Error: ${e.message}")
                    }
                }
                query.limit(pageSize, offset.toLong())
                    .map(::resultRowToIntervention).toList()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<Intervention>()
        }
    }

    override suspend fun getAllInterventions(): List<Intervention> {
        return try{
            transaction {
                Interventions.selectAll()
                    .map(::resultRowToIntervention).toList()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<Intervention>()
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Report Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun addReport(
        clientId: Int,
        location: String,
        date: LocalDateTime,
        status: Report.ReportStatus
    ): Int {
        return try{
            transaction {
                val insertStatement = Reports.insert {
                    it[Reports.client_id] = clientId
                    it[Reports.location] = location
                    it[Reports.date] = date
                    it[Reports.status] = status.status.toShort()
                }
                insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToReport)?.id ?: -1
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            -1
        }
    }

    override suspend fun deleteReport(id: Int): Boolean {
        return try{
            transaction { Reports.deleteWhere { Reports.id eq id } > 0 }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun updateReportLocation(id: Int, location: String): Boolean {
        return try{
            transaction {
                Reports.update({ Reports.id eq id }) {
                    it[Reports.location] = location
                } > 0
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun changeReportStatus(id: Int, status: Report.ReportStatus): Report? {
        return try{
            transaction {
                val success = Reports.update({ Reports.id eq id }) {
                    it[Reports.status] = status.status.toShort()
                } > 0
                if (success) {
                    return@transaction Reports
                        .selectAll().where { Reports.id eq id }
                        .mapNotNull(::resultRowToReport)
                        .singleOrNull()
                }
                return@transaction null
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getReport(id: Int): Report? {
        return try{
            transaction {
                Reports
                    .selectAll().where { Reports.id eq id }
                    .mapNotNull(::resultRowToReport)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }


    override suspend fun getReports(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>?,
        filterValue: String?,
        filterType: String?,
        sortBy: Column<out Any>?,
        sortDir: String?
    ): List<Report> {
        return try{
            transaction {
                val offset = (page - 1) * pageSize

                val query = Reports.selectAll().apply {
                    if (filterColumn != null && filterValue != null) {
                        try {
                            if (filterColumn.columnType is StringColumnType) {
                                val column = filterColumn as? Column<String>
                                if (column != null)
                                    when (filterType) {
                                        "contains" -> where { column like "%$filterValue%" }
                                        "startsWith" -> where { column like "$filterValue%" }
                                        "endsWith" -> where { column like "%$filterValue" }
                                    }
                            } else {
                                val column = filterColumn as? Column<Any>
                                if (column != null)
                                    when (filterType) {
                                        "equals" -> where { column eq filterValue }
                                        "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                        "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                        "isAnyOf" -> where { column eq filterValue }
                                    }
                            }
                        } catch (e: Error) {
                            System.err.println("Error: ${e.message}")
                        }
                    }
                }
                if (sortBy != null) {
                    try {
                        query.orderBy(sortBy, if (sortDir == "desc") SortOrder.DESC else SortOrder.ASC)
                    } catch (e: Error) {
                        System.err.println("Error: ${e.message}")
                    }
                }
                query.limit(pageSize, offset.toLong())
                    .map(::resultRowToReport).toList()

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<Report>()
        }
    }

    override suspend fun getAllReports(filterFinished: Boolean): List<Report> {
        return try{
            transaction {
                Reports.selectAll().let { query ->
                    if (filterFinished) {
                        query.where { (Reports.status eq 0) or (Reports.status eq 1) }
                    } else {
                        query
                    }
                }.map(::resultRowToReport).toList()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<Report>()
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Guard Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun isGuardLoginUsed(login: String): Boolean {
        return try{
            transaction {
                Guards
                    .selectAll().where { Guards.login eq login }
                    .mapNotNull(::resultRowToGuard)
                    .singleOrNull() != null
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            true
        }
    }

    override suspend fun addGuard(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        email: String
    ): Triple<Boolean, String, Guard?> {
        return try{
            transaction {

                val conflict = Guards.selectAll().where {
                    (Guards.login eq login) or (Guards.phone eq phone)
                }.firstOrNull()

                if (conflict != null) {
                    val message = when {
                        conflict[Guards.login] == login -> "Login is already taken."
                        conflict[Guards.phone] == phone -> "Phone is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction Triple(false, message, null)
                }

                val insertStatement = Guards.insert {
                    it[Guards.login] = login
                    it[Guards.password] = HashPassword.hashPassword(password)
                    it[Guards.name] = name
                    it[Guards.surname] = surname
                    it[Guards.phone] = phone
                    it[Guards.email] = email
                    it[Guards.account_deleted] = false
                }
                if (insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToGuard) != null)
                    return@transaction Triple(
                        true,
                        "Guard created successfully.",
                        insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToGuard(resultRow) })
                return@transaction Triple(false, "Failed to create guard.", null)
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            Triple(false, "An error occurred: ${e.message}", null)
        }
    }

    override suspend fun editGuard(
        id: Int,
        login: String?,
        password: String,
        newPassword: String?,
        name: String?,
        surname: String?,
        phone: String?,
        email: String?
    ): Pair<String, Guard?> {
        return try{
            transaction {

                val guardExists = Guards.selectAll().where { (Guards.id eq id) }.singleOrNull()
                if (guardExists == null) {
                    return@transaction "Incorrect Id for the Guard." to null
                }

                if (!comparePasswords(password, resultRowToGuard(guardExists).password))
                    return@transaction "Incorrect password for the guard." to null


                val conditions = mutableListOf<Op<Boolean>>()
                if (login != null) conditions.add(Guards.login eq login)
                if (phone != null) conditions.add(Guards.phone eq phone)

                val conflict = if (conditions.isNotEmpty()) {
                    Guards.selectAll().where {
                        conditions.reduce { acc, condition -> acc or condition } and (Guards.id neq id)
                    }.firstOrNull()
                } else {
                    null
                }

                if (conflict != null) {
                    val message = when {
                        conflict[Guards.login] == login -> "Login is already taken."
                        conflict[Guards.phone] == phone -> "Phone is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction message to null
                }

                val updated = Guards.update({ Guards.id eq id }) {
                    login?.let { login -> it[Guards.login] = login }
                    newPassword?.let { newPassword -> it[Guards.password] = newPassword }
                    name?.let { name -> it[Guards.name] = name }
                    surname?.let { surname -> it[Guards.surname] = surname }
                    phone?.let { phone -> it[Guards.phone] = phone }
                    email?.let { email -> it[Guards.email] = email }
                } > 0

                if (updated) {
                    val guard = Guards.selectAll().where({ Guards.id eq id }).mapNotNull(::resultRowToGuard)
                        .singleOrNull()
                    notifyClientsAboutChanges("guards", id)
                    return@transaction "Guard updated successfully." to guard
                }
                return@transaction "Failed to update guard." to null
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            "An error occurred: ${e.message}" to null
        }
    }

    override suspend fun editGuard(
        id: Int,
        name: String?,
        surname: String?,
        phone: String?,
        email: String?,
        isActive: Boolean?
    ): Pair<Boolean, String> {
        return try{
            transaction {

                val guardExists = Guards.selectAll().where { (Guards.id eq id) }.singleOrNull()
                if (guardExists == null) {
                    return@transaction false to "Incorrect Id for the Guard."
                }

                if (phone != null) {
                    val list = Guards.selectAll().where { Guards.phone eq phone }.mapNotNull(::resultRowToGuard)
                    if (list.count() > 0 && list.any { guard -> guard.id != id }) {
                        return@transaction Pair(false, "Phone is already taken.")
                    }
                }

                val updated = Guards.update({ Guards.id eq id }) {
                    name?.let { name -> it[Guards.name] = name }
                    surname?.let { surname -> it[Guards.surname] = surname }
                    phone?.let { phone -> it[Guards.phone] = phone }
                    email?.let { email -> it[Guards.email] = email }
                    isActive?.let { isActive -> it[Guards.account_deleted] = !isActive }
                } > 0

                if (updated) {
                    notifyClientsAboutChanges("guards", id)
                    return@transaction true to "Guard updated successfully."
                }
                return@transaction false to "Failed to update guard."
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false to "An error occurred: ${e.message}"
        }
    }

    override suspend fun changeGuardPassword(
        id: Int,
        password: String
    ): Pair<Boolean, String> {
        return try{
            transaction {

                val guardExists = Guards.selectAll().where { (Guards.id eq id) }.singleOrNull()
                if (guardExists == null) {
                    return@transaction false to "Incorrect Id for the guard."
                }

                val updated = Guards.update({ Guards.id eq id }) {
                    it[Guards.password] = HashPassword.hashPassword(password)
                } > 0
                if (updated)
                    return@transaction true to "Guard updated successfully."
                return@transaction false to "Failed to update guard."
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false to "An error occurred: ${e.message}"
        }
    }

    override suspend fun deleteGuard(id: Int): Boolean {
        return try{
            transaction {
                val ret = Guards.update({ Guards.id eq id }) {
                    it[Guards.account_deleted] = true
                } > 0
                if (ret)
                    notifyClientsAboutChanges("guards", id)
                return@transaction ret
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun restoreGuard(id: Int): Boolean {
        return try{
            transaction {
                val ret = Guards.update({ Guards.id eq id }) {
                    it[Guards.account_deleted] = false
                } > 0
                if (ret)
                    notifyClientsAboutChanges("guards", id)
                return@transaction ret
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun getGuard(id: Int): Guard? {
        return try {
            transaction {
                Guards
                    .selectAll().where { Guards.id eq id }
                    .mapNotNull(::resultRowToGuard)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
           null
        }
    }

    override suspend fun getGuard(email: String): Guard? {
        return try{
            transaction {
                Guards
                    .selectAll().where { Guards.email eq email }
                    .mapNotNull(::resultRowToGuard)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getGuard(login: String, password: String): Pair<String, Guard?> {
        return try{
            transaction {
                val guard = Guards
                    .selectAll().where { (Guards.login eq login) }
                    .mapNotNull(::resultRowToGuard)
                    .singleOrNull()
                if (guard == null)
                    return@transaction "There is no guard with this Login." to null
                if (!comparePasswords(password, guard.password))
                    return@transaction "Incorrect password for the guard." to null
                return@transaction "Success" to guard
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            "An error occurred: ${e.message}" to null
        }
    }

    override suspend fun getGuards(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>?,
        filterValue: String?,
        filterType: String?,
        sortBy: Column<out Any>?,
        sortDir: String?
    ): List<GuardInfo> {
        return try{
            transaction {
                val offset = (page - 1) * pageSize
                val query = Guards.selectAll().apply {
                    if (filterColumn != null && filterValue != null) {
                        try {
                            if (filterColumn.columnType is StringColumnType) {
                                val column = filterColumn as? Column<String>
                                if (column != null)
                                    when (filterType) {
                                        "contains" -> where { column like "%$filterValue%" }
                                        "startsWith" -> where { column like "$filterValue%" }
                                        "endsWith" -> where { column like "%$filterValue" }
                                    }
                            } else {
                                val column = filterColumn as? Column<Any>
                                if (column != null)
                                    when (filterType) {
                                        "equals" -> where { column eq filterValue }
                                        "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                        "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                        "isAnyOf" -> where { column eq filterValue }
                                    }
                            }
                        } catch (e: Error) {
                            System.err.println("Error: ${e.message}")
                        }
                    }
                }
                if (sortBy != null) {
                    try {
                        query.orderBy(sortBy, if (sortDir == "desc") SortOrder.DESC else SortOrder.ASC)
                    } catch (e: Error) {
                        System.err.println("Error: ${e.message}")
                    }
                }
                query.limit(pageSize, offset.toLong())
                    .map(::resultRowToGuardInfo).toList()

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<GuardInfo>()
        }
    }

    override suspend fun getGuards(ids: List<Int>): List<GuardInfo> {
        return try{
            transaction {
                return@transaction Guards
                    .selectAll().where { Guards.id inList ids }
                    .mapNotNull(::resultRowToGuardInfo)
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<GuardInfo>()
        }
    }

    override suspend fun getAllGuards(): List<GuardInfo> {
        return try{
            transaction {
                Guards.selectAll()
                    .map(::resultRowToGuardInfo).toList()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<GuardInfo>()
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Employee Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun addEmployee(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        email: String,
        role: Employee.Role
    ): Triple<Boolean, String, Employee?> {
        return try{
            transaction {

                val conflict = Employees.selectAll().where {
                    (Employees.login eq login) or (Employees.phone eq phone) or (Employees.email eq email)
                }.firstOrNull()

                if (conflict != null) {
                    val message = when {
                        conflict[Employees.login] == login -> "Login is already taken."
                        conflict[Employees.phone] == phone -> "Phone is already taken."
                        conflict[Employees.email] == phone -> "Email is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction Triple(false, message, null)
                }

                val insertStatement = Employees.insert {
                    it[Employees.login] = login
                    it[Employees.password] = HashPassword.hashPassword(password)
                    it[Employees.name] = name
                    it[Employees.surname] = surname
                    it[Employees.phone] = phone
                    it[Employees.email] = email
                    it[Employees.role] = role.role.toShort()
                    it[Employees.account_deleted] = false
                }

                if (insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToEmployee) != null)
                    return@transaction Triple(
                        true,
                        "Employee created successfully.",
                        insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToEmployee(resultRow) })
                return@transaction Triple(false, "Failed to create employee.", null)
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            Triple(false, "An error occurred: ${e.message}", null)
        }
    }

    override suspend fun editEmployee(
        id: Int,
        login: String?,
        password: String,
        newPassword: String?,
        name: String?,
        surname: String?,
        phone: String?,
        email: String?,
        role: Employee.Role?
    ): Triple<Boolean, String, EmployeeInfo?> {
        return try{
            transaction {

                val employeeExists = Employees.selectAll().where { (Employees.id eq id) }.singleOrNull()
                if (employeeExists == null) {
                    return@transaction Triple(false, "Incorrect Id for the employee.", null)
                }

                val conditions = mutableListOf<Op<Boolean>>()
                if (login != null) conditions.add(Employees.login eq login)
                if (phone != null) conditions.add(Employees.phone eq phone)
                if (email != null) conditions.add(Employees.email eq email)

                val conflict = if (conditions.isNotEmpty()) {
                    Employees.selectAll().where {
                        conditions.reduce { acc, condition -> acc or condition } and (Employees.id neq id) // check only for different accounts
                    }.firstOrNull()
                } else {
                    null
                }

                if (conflict != null) {
                    val message = when {
                        conflict[Employees.login] == login -> "Login is already taken."
                        conflict[Employees.phone] == phone -> "Phone is already taken."
                        conflict[Employees.email] == phone -> "Email is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction Triple(false, message, null)
                }

                if (!comparePasswords(password, resultRowToEmployee(employeeExists).password))
                    return@transaction Triple(false, "Incorrect password for the employee.", null)

                val updated = Employees.update({ Employees.id eq id }) {
                    login?.let { login -> it[Employees.login] = login }
                    newPassword?.let { newPassword -> it[Employees.password] = HashPassword.hashPassword(newPassword) }
                    name?.let { name -> it[Employees.name] = name }
                    surname?.let { surname -> it[Employees.surname] = surname }
                    phone?.let { phone -> it[Employees.phone] = phone }
                    email?.let { email -> it[Employees.email] = email }
                    role?.let { role -> it[Employees.role] = role.role.toShort() }
                } > 0
                if (updated) {
                    notifyClientsAboutChanges("employees", id)
                    val employeeInfo = runBlocking { getEmployee(id)?.toEmployeeInfo() }
                    return@transaction Triple(true, "Employee updated successfully.", employeeInfo)
                }
                return@transaction Triple(false, "Failed to update employee.", null)
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            Triple(false, "An error occurred: ${e.message}", null)
        }
    }

    override suspend fun editEmployee(
        id: Int,
        name: String?,
        surname: String?,
        phone: String?,
        email: String?,
        role: Employee.Role?,
        isActive: Boolean?
    ): Pair<Boolean, String> {
        return try{
            transaction {

                val employeeExists = Employees.selectAll().where { (Employees.id eq id) }.singleOrNull()
                if (employeeExists == null) {
                    return@transaction false to "Incorrect Id for the employee."
                }

                val conditions = mutableListOf<Op<Boolean>>()
                if (phone != null) conditions.add(Employees.phone eq phone)
                if (email != null) conditions.add(Employees.email eq email)

                val conflict = if (conditions.isNotEmpty()) {
                    Employees.selectAll().where {
                        conditions.reduce { acc, condition -> acc or condition } and (Employees.id neq id) // check only for different accounts
                    }.firstOrNull()
                } else {
                    null
                }

                if (conflict != null) {
                    val message = when {
                        conflict[Employees.phone] == phone -> "Phone is already taken."
                        conflict[Employees.email] == phone -> "Email is already taken."
                        else -> "Conflict detected."
                    }
                    return@transaction Pair(false, message)
                }

                val updated = Employees.update({ Employees.id eq id }) {
                    name?.let { name -> it[Employees.name] = name }
                    surname?.let { surname -> it[Employees.surname] = surname }
                    phone?.let { phone -> it[Employees.phone] = phone }
                    email?.let { email -> it[Employees.email] = email }
                    role?.let { role -> it[Employees.role] = role.role.toShort() }
                    isActive?.let { isActive -> it[Employees.account_deleted] = !isActive }
                } > 0
                if (updated) {
                    notifyClientsAboutChanges("employees", id)
                    return@transaction true to "Employee updated successfully."
                }
                return@transaction false to "Failed to update employee."
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false to "An error occurred: ${e.message}"
        }
    }

    override suspend fun changeEmployeeRole(id: Int, role: Employee.Role): Pair<Boolean, String> {
        return try{
            transaction {

                val employeeExists = Employees.selectAll().where { (Employees.id eq id) }.singleOrNull()
                if (employeeExists == null) {
                    return@transaction false to "Incorrect Id for the employee."
                }

                val updated = Employees.update({ Employees.id eq id }) {
                    it[Employees.role] = role.role.toShort()
                } > 0
                if (updated) {
                    notifyClientsAboutChanges("employees", id)
                    return@transaction true to "Employee updated successfully."
                }
                return@transaction false to "Failed to update employee."
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false to "An error occurred: ${e.message}"
        }
    }

    override suspend fun changeEmployeePassword(id: Int, password: String): Pair<Boolean, String> {
        return try{
            transaction {

                val employeeExists = Employees.selectAll().where { (Employees.id eq id) }.singleOrNull()
                if (employeeExists == null) {
                    return@transaction false to "Incorrect Id for the employee."
                }

                val updated = Employees.update({ Employees.id eq id }) {
                    it[Employees.password] = HashPassword.hashPassword(password)
                } > 0
                if (updated)
                    return@transaction true to "Employee updated successfully."
                return@transaction false to "Failed to update employee."
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false to "An error occurred: ${e.message}"
        }
    }

    override suspend fun getEmployee(id: Int): Employee? {
        return try{
            transaction {
                Employees
                    .selectAll().where { Employees.id eq id }
                    .mapNotNull(::resultRowToEmployee)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getEmployee(email: String): Employee? {
        return try{
            transaction {
                Employees
                    .selectAll().where { Employees.email eq email }
                    .mapNotNull(::resultRowToEmployee)
                    .singleOrNull()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            null
        }
    }

    override suspend fun getEmployee(login: String, password: String): Pair<String, Employee?> {
        return try{
            transaction {
                val employee = Employees
                    .selectAll().where { (Employees.login eq login) }
                    .mapNotNull(::resultRowToEmployee)
                    .singleOrNull()
                if (employee == null) {
                    return@transaction "Incorrect Id for the employee." to null
                }
                if (!comparePasswords(password, employee.password))
                    return@transaction "Incorrect password for the employee." to null
                return@transaction "Success" to employee

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            "An error occurred: ${e.message}" to null
        }
    }

    override suspend fun getEmployees(
        page: Int,
        pageSize: Int,
        filterColumn: Column<out Any>?,
        filterValue: String?,
        filterType: String?,
        sortBy: Column<out Any>?,
        sortDir: String?
    ): List<EmployeeInfo> {
        return try{
            transaction {
                val offset = (page - 1) * pageSize
                val query = Employees.selectAll().apply {
                    if (filterColumn != null && filterValue != null) {
                        try {

                            if (filterColumn.columnType is StringColumnType) {
                                val column = filterColumn as? Column<String>
                                if (column != null)
                                    when (filterType) {
                                        "contains" -> where { column like "%$filterValue%" }
                                        "startsWith" -> where { column like "$filterValue%" }
                                        "endsWith" -> where { column like "%$filterValue" }
                                    }
                            } else {
                                val column = filterColumn as? Column<Any>
                                if (column != null)
                                    when (filterType) {
                                        "equals" -> where { column eq filterValue }
                                        "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                        "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                        "isAnyOf" -> where { column eq filterValue }
                                    }
                            }
                        } catch (e: Error) {
                            System.err.println("Error: ${e.message}")
                        }
                    }
                }
                if (sortBy != null) {
                    try {
                        query.orderBy(sortBy, if (sortDir == "desc") SortOrder.DESC else SortOrder.ASC)
                    } catch (e: Error) {
                        System.err.println("Error: ${e.message}")
                    }
                }
                query.limit(pageSize, offset.toLong())
                    .map(::resultRowToEmployeeInfo).toList()
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<EmployeeInfo>()
        }
    }

    override suspend fun getEmployees(ids: List<Int>): List<EmployeeInfo> {
        return try{
            transaction {
                return@transaction Employees
                    .selectAll().where { Employees.id inList ids }
                    .mapNotNull(::resultRowToEmployeeInfo)
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<EmployeeInfo>()
        }
    }

    override suspend fun getAllEmployees(): List<EmployeeInfo> {
        return try{
            transaction {

                Employees.selectAll()
                    .map(::resultRowToEmployeeInfo).toList()

            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            emptyList<EmployeeInfo>()
        }
    }

    override suspend fun deleteEmployee(id: Int): Boolean {
        return try{
            transaction {
                val ret = Employees.update({ Employees.id eq id }) {
                    it[Employees.account_deleted] = true
                } > 0
                if (ret)
                    notifyClientsAboutChanges("employees", id)
                return@transaction ret
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }

    override suspend fun restoreEmployee(id: Int): Boolean {
        return try{
            transaction {
                val ret = Employees.update({ Employees.id eq id }) {
                    it[Employees.account_deleted] = false
                } > 0
                if (ret)
                    notifyClientsAboutChanges("employees", id)
                return@transaction ret
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            false
        }
    }
}
