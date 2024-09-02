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

object DaoMethods:DaoMethodsInterface {

    private fun resultRowToUser(row: ResultRow) = Customer(
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
        report_id = row[Interventions.reportId],
        guard_id = row[Interventions.guardId],
        dispatcher_id = row[Interventions.dispatcherId],
        patrol_number = row[Interventions.patrolNumber]
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
        name = row[Guards.name],
        surname = row[Guards.surname],
        phone = row[Guards.phone],
        statusCode = Guard.GuardStatus.UNAVAILABLE.status,
        location = ""
    )

    private fun resultRowToDispatcher(row: ResultRow) = Employee(
        id = row[Employees.id],
        name = row[Employees.name],
        surname = row[Employees.surname],
        password = row[Employees.password],
        phone = row[Employees.phone],
        roleCode = row[Employees.role]
    )




    //Client

    override suspend fun addClient(
        login: String,
        password: String,
        phone: String,
        pesel: String,
        email: String
    ): Boolean {
        return transaction {
            val insertStatement = Customers.insert {
                it[Customers.login] = login
                it[Customers.password] = password
                it[Customers.phone] = phone
                it[Customers.pesel] = pesel
                it[Customers.email] = email
                it[Customers.account_deleted] = false
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser) != null
        }
    }

    override suspend fun editClient(
        id: Int,
        login: String?,
        password: String?,
        phone: String?,
        pesel: String?,
        email: String?
    ): Boolean {
        return transaction {
            Customers.update({ Customers.id eq id }) {
                login?.let { firstName -> it[Customers.login] = firstName }
                password?.let { password -> it[Customers.password] = password }
                phone?.let { phone -> it[Customers.phone] = phone }
                pesel?.let { pesel -> it[Customers.pesel] = pesel }
                email?.let { email -> it[Customers.email] = email }
            } > 0
        }
    }

    override suspend fun deleteClient(id: Int): Boolean {
        return transaction {
            Customers.update({ Customers.id eq id }) {
                it[Customers.account_deleted] = true
            } > 0
        }
    }

    override suspend fun getClient(id: Int): Customer? {
        return transaction {
            Customers
                .selectAll().where { Customers.id eq id }
                .mapNotNull(::resultRowToUser)
                .singleOrNull()
        }
    }

    override suspend fun getAllClients(page: Int, pageSize: Int): List<Customer>{
        return transaction {
            val offset = (page - 1) * pageSize

            Customers.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToUser).toList()
        }

    }






    //Intervention

    override suspend fun addIntervention(reportId: Int, guardId: Int, dispatcherId: Int, patrolNumber: Int): Boolean {
        return transaction {
            val insertStatement = Interventions.insert {
                it[Interventions.reportId] = reportId
                it[Interventions.guardId] = guardId
                it[Interventions.dispatcherId] = dispatcherId
                it[Interventions.patrolNumber] = patrolNumber
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

    override suspend fun addGuard(name: String, surname: String, phone: String): Boolean {
        return transaction {
            val insertStatement = Guards.insert {
                it[Guards.name] = name
                it[Guards.surname] = surname
                it[Guards.phone] = phone
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToGuard) != null
        }
    }

    override suspend fun editGuard(
        id: Int,
        name: String?,
        surname: String?,
        phone: String?
    ): Boolean {
        return transaction {
            Guards.update({ Guards.id eq id }) {
                name?.let { name -> it[Guards.name] = name }
                surname?.let { surname -> it[Guards.surname] = surname }
                phone?.let { phone -> it[Guards.phone] = phone }
            } > 0
        }
    }

    override suspend fun deleteGuard(id: Int): Boolean {
        return transaction { Guards.deleteWhere { Guards.id eq id } > 0}
    }


    override suspend fun getGuard(id: Int): Guard? {
        return transaction {
            Guards
                .selectAll().where { Guards.id eq id }
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






    //Dispatcher

    override suspend fun addDispatcher(name: String, surname: String, password: String, phone: String, role: Employee.Role): Boolean {
        return transaction {
            val insertStatement = Employees.insert {
                it[Employees.name] = name
                it[Employees.surname] = surname
                it[Employees.password] = password
                it[Employees.phone] = phone
                it[Employees.role] = role.role.toShort()
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToDispatcher) != null
        }
    }

    override suspend fun deleteDispatcher(id: Int): Boolean {
        return transaction { Employees.deleteWhere { Employees.id eq id } > 0}
    }

    override suspend fun editDispatcher(id: Int, name: String?, surname: String?,password: String?, phone: String?, role:Employee.Role?): Boolean {
        return transaction {
            Employees.update({ Employees.id eq id }) {
                name?.let { name -> it[Employees.name] = name }
                surname?.let { surname -> it[Employees.surname] = surname }
                password?.let { password -> it[Employees.password] = password }
                phone?.let { phone -> it[Employees.phone] = phone }
                role?.let{role-> it[Employees.role] = role.role.toShort()}
            } > 0
        }
    }

    override suspend fun getDispatcher(id: Int): Employee? {
        return transaction {
            Employees
                .selectAll().where { Employees.id eq id }
                .mapNotNull(::resultRowToDispatcher)
                .singleOrNull()
        }
    }

    override suspend fun getAlDispatchers(page:Int, pageSize: Int): List<Employee> {
        return transaction {
            val offset = (page - 1) * pageSize

            Employees.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToDispatcher).toList()

        }
    }

}
