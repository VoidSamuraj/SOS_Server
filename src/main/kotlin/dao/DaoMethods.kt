package dao

import Customer
import Guard
import Guards
import Intervention
import Interventions
import Report
import Reports
import Customers
import Employee
import Employees
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.mapNotNull

object DaoMethods:DaoMethodsInterface {

    private fun resultRowToClient(row: ResultRow) = Customer(
        id = row[Customers.id],
        login = row[Customers.login],
        password = row[Customers.password],
        phone = row[Customers.phone],
        pesel = row[Customers.pesel],
        email = row[Customers.email],
        account_deleted = row[Customers.account_deleted]
    )

    private fun resultRowToIntervention(row: ResultRow) = Intervention(
        id = row[Interventions.id],
        report_id = row[Interventions.report_id],
        guard_id = row[Interventions.guard_id],
        employee_id = row[Interventions.employee_id],
        start_time = row[Interventions.start_time],
        end_time = row[Interventions.end_time],
        statusCode = row[Interventions.status],
        patrol_number = row[Interventions.patrol_number]
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
        roleCode = row[Employees.role],
        account_deleted = row[Employees.account_deleted]
    )




    //Customer

    override suspend fun addCustomer(
        login: String,
        password: String,
        phone: String,
        pesel: String,
        email: String
    ): Triple<Boolean,String, Customer?> {
        return transaction {
            if (Customers.select(Customers.login).where{ Customers.login eq login}.count() > 0) {
                return@transaction Triple(false, "Login is already taken.", null)
            }
            if (Customers.select(Customers.phone).where{ Customers.phone eq phone}.count() > 0) {
                return@transaction Triple(false, "Phone is already taken.", null)
            }
            if (Customers.select(Customers.pesel).where{ Customers.pesel eq pesel}.count() > 0) {
                return@transaction Triple(false, "Pesel is already taken.", null)
            }
            if (Customers.select(Customers.email).where{ Customers.email eq email}.count() > 0) {
                return@transaction Triple(false, "Email is already taken.", null)
            }

            val insertStatement = Customers.insert {
                it[Customers.login] = login
                it[Customers.password] = password
                it[Customers.phone] = phone
                it[Customers.pesel] = pesel
                it[Customers.email] = email
                it[Customers.account_deleted] = false
            }
            if(insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToClient) != null){
                Triple(true, "Customer created successfully.", insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToClient(resultRow) })
            } else {
                Triple(false, "Failed to create client.", null)
            }
        }
    }

    override suspend fun editCustomer(
        id: Int,
        login: String?,
        password: String,
        newPassword: String?,
        phone: String?,
        pesel: String?,
        email: String?
    ): Pair<Boolean,String> {
        return transaction {

            val customerExists = Customers.select(Customers.login, Customers.password).where{(Customers.id eq id) and (Customers.password eq password)}.singleOrNull()
            if (customerExists == null) {
                return@transaction false to "Incorrect password for the Customer."
            }

            val result = Customers.update({ Customers.id eq id }) {
                login?.let { firstName -> it[Customers.login] = firstName }
                newPassword?.let { newPassword -> it[Customers.password] = newPassword }
                phone?.let { phone -> it[Customers.phone] = phone }
                pesel?.let { pesel -> it[Customers.pesel] = pesel }
                email?.let { email -> it[Customers.email] = email }
            } > 0
            if(result){
                true to "Customer updated successfully."
            } else {
                false to "Failed to update customer."
            }
        }
    }

    override suspend fun deleteCustomer(id: Int): Boolean {
        return transaction {
            Customers.update({ Customers.id eq id }) {
                it[Customers.account_deleted] = true
            } > 0
        }
    }

    override suspend fun getCustomer(id: Int): Customer? {
        return transaction {
            Customers
                .selectAll().where { Customers.id eq id }
                .mapNotNull(::resultRowToClient)
                .singleOrNull()
        }
    }

    override suspend fun getCustomer(login:String, password: String): Customer? {
        return transaction {
            Customers
                .selectAll().where { (Customers.login eq id) and (Customers.password eq password) }
                .mapNotNull(::resultRowToClient)
                .singleOrNull()
        }
    }

    override suspend fun getAllCustomers(page: Int, pageSize: Int): List<Customer>{
        return transaction {
            val offset = (page - 1) * pageSize

            Customers.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToClient).toList()
        }

    }






    //Intervention

    override suspend fun addIntervention(report_id: Int, guard_id: Int, employee_id: Int, start_time: LocalDateTime, end_time: LocalDateTime, status:Intervention.InterventionStatus, patrol_number: Int):Boolean {
        return transaction {
            val insertStatement = Interventions.insert {
                it[Interventions.report_id] = report_id
                it[Interventions.guard_id] = guard_id
                it[Interventions.employee_id] = employee_id
                it[Interventions.start_time] = start_time
                it[Interventions.end_time] = end_time
                it[Interventions.status] = status.status.toShort()
                it[Interventions.patrol_number] = patrol_number
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToIntervention) != null
        }
    }

    override suspend fun getIntervention(id: Int): Intervention? {
        return transaction {
            Interventions
                .selectAll().where { Interventions.id eq id }
                .mapNotNull(::resultRowToIntervention)
                .singleOrNull()
        }
    }

    override suspend fun getAllInterventions(page: Int, pageSize: Int): List<Intervention> {
        return transaction {
            val offset = (page - 1) * pageSize

            Interventions.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToIntervention).toList()
        }
    }







    //Report

    override suspend fun addReport(
        clientId: Int,
        location: String,
        date: LocalDateTime,
        status: Report.ReportStatus
    ): Boolean {
        return transaction {
            val insertStatement = Reports.insert {
                it[Reports.client_id] = clientId
                it[Reports.location] = location
                it[Reports.date] = date
                it[Reports.status] = status.status.toShort()
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToReport) != null
        }
    }

    override suspend fun deleteReport(id: Int): Boolean {
        return transaction { Reports.deleteWhere { Reports.id eq id } > 0}
    }

    override suspend fun updateReportLocation(id: Int, location: String): Boolean {
        return transaction {
            Reports.update({ Reports.id eq id }) {
                it[Reports.location] = location
            } > 0
        }
    }

    override suspend fun changeReportStatus(id: Int, status: Report.ReportStatus): Boolean {
        return transaction {
            Reports.update({ Reports.id eq id }) {
                it[Reports.status] = status.status.toShort()
            } > 0
        }
    }

    override suspend fun getReport(id: Int): Report? {
        return transaction {
            Reports
                .selectAll().where { Reports.id eq id }
                .mapNotNull(::resultRowToReport)
                .singleOrNull()
        }
    }

    override suspend fun getAllReports(page:Int, pageSize: Int): List<Report> {
        return transaction {
            val offset = (page - 1) * pageSize

            Reports.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToReport).toList()

        }
    }







    //Guard

    override suspend fun addGuard( login: String, password: String, name: String, surname: String, phone: String): Triple<Boolean, String, Guard?> {
        return transaction {

            if (Guards.select(Guards.login).where{ Guards.login eq login}.count() > 0) {
                return@transaction Triple(false, "Login is already taken.", null)
            }
            if (Guards.select(Guards.phone).where{ Guards.phone eq phone}.count() > 0) {
                return@transaction Triple(false, "Phone is already taken.", null)
            }

            val insertStatement = Guards.insert {
                it[Guards.login] = login
                it[Guards.password] = password
                it[Guards.name] = name
                it[Guards.surname] = surname
                it[Guards.phone] = phone
                it[Guards.account_deleted] = false
            }
            if(insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToGuard) != null){
                Triple(true, "Guard created successfully.", insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToGuard(resultRow) })
            } else {
                Triple(false, "Failed to create guard.", null)
            }
        }
    }

    override suspend fun editGuard(
        id: Int,
        login: String?,
        password: String,
        newPassword: String?,
        name: String?,
        surname: String?,
        phone: String?
    ): Pair<Boolean, String> {
        return transaction {

            val guardExists = Guards.select(Guards.login, Guards.password).where {(Guards.id eq id) and (Guards.password eq password) }.singleOrNull()
            if (guardExists == null) {
                return@transaction false to "Incorrect password for the Guard."
            }

            val updated = Guards.update({ Guards.id eq id }) {
                login?.let{ login -> it[Guards.login] = login}
                newPassword?.let{ newPassword -> it[Guards.password] = newPassword}
                name?.let { name -> it[Guards.name] = name }
                surname?.let { surname -> it[Guards.surname] = surname }
                phone?.let { phone -> it[Guards.phone] = phone }
            } > 0

            if (updated) {
                true to "Guard updated successfully."
            } else {
                false to "Failed to update guard."
            }
        }
    }

    override suspend fun deleteGuard(id: Int): Boolean {
        return transaction {
            Guards.update({ Guards.id eq id }) {
                it[Guards.account_deleted] = true
            } > 0
        }
    }


    override suspend fun getGuard(id: Int): Guard? {
        return transaction {
            Guards
                .selectAll().where { Guards.id eq id }
                .mapNotNull(::resultRowToGuard)
                .singleOrNull()
        }
    }


    override suspend fun getGuard(login:String, password: String): Guard? {
        return transaction {
            Guards
                .selectAll().where { (Guards.login eq login) and (Guards.password eq password) }
                .mapNotNull(::resultRowToGuard)
                .singleOrNull()
        }
    }

    override suspend fun getAllGuards(page:Int, pageSize: Int): List<Guard> {
        return transaction {
            val offset = (page - 1) * pageSize

            Guards.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToGuard).toList()
        }
    }






    //Employee

    override suspend fun addEmployee(login: String, password: String, name: String, surname: String, phone: String, role: Employee.Role): Triple<Boolean,String,Employee?> {
        return transaction {

            if (Employees.select(Employees.login).where{ Employees.login eq login}.count() > 0) {
                return@transaction Triple(false, "Login is already taken.",null)
            }
            if (Employees.select(Employees.phone).where{ Employees.phone eq phone}.count() > 0) {
                return@transaction Triple(false, "Phone is already taken.",null)
            }

            val insertStatement = Employees.insert {
                it[Employees.login] = login
                it[Employees.password] = password
                it[Employees.name] = name
                it[Employees.surname] = surname
                it[Employees.phone] = phone
                it[Employees.role] = role.role.toShort()
                it[Employees.account_deleted] = false
            }

            if (insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToEmployee) != null) {
                Triple(true, "Employee created successfully.",insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToEmployee(resultRow) })
            } else {
                Triple(false, "Failed to create employee.",null)
            }
        }
    }

    override suspend fun deleteEmployee(id: Int): Boolean {
        return transaction {
            Employees.update({ Employees.id eq id }) {
                it[Employees.account_deleted] = true
            } > 0
        }
    }

    override suspend fun editEmployee(id: Int, login: String?, password: String, newPassword: String?, name: String?, surname: String?, phone: String?, role:Employee.Role?): Pair<Boolean, String> {
        return transaction {

            val employeeExists = Employees.select(Employees.id, Employees.password).where{ (Employees.id eq id) and (Employees.password eq password) }.singleOrNull()
            if (employeeExists == null) {
                return@transaction false to "Incorrect password for the employee."
            }

            val updated = Employees.update({ Employees.id eq id }) {
                login?.let { login -> it[Employees.login] = login }
                newPassword?.let { newPassword -> it[Employees.password] = newPassword }
                name?.let { name -> it[Employees.name] = name }
                surname?.let { surname -> it[Employees.surname] = surname }
                phone?.let { phone -> it[Employees.phone] = phone }
                role?.let{role-> it[Employees.role] = role.role.toShort()}
            } > 0
            if (updated) {
                true to "Employee updated successfully."
            } else {
                false to "Failed to update employee."
            }
        }
    }

    override suspend fun getEmployee(id: Int): Employee? {
        return transaction {
            Employees
                .selectAll().where { Employees.id eq id }
                .mapNotNull(::resultRowToEmployee)
                .singleOrNull()
        }
    }

    override suspend fun getEmployee(login:String, password: String): Employee? {
        return transaction {
            Employees
                .selectAll().where { (Employees.login eq login) and (Employees.password eq password) }
                .mapNotNull(::resultRowToEmployee)
                .singleOrNull()
        }
    }

    override suspend fun getAllEmployees(page:Int, pageSize: Int): List<Employee> {
        return transaction {
            val offset = (page - 1) * pageSize

            Employees.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToEmployee).toList()

        }
    }

}
