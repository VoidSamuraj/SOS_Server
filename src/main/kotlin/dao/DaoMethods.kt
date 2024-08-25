package dao

import SystemClient
import Guard
import Guards
import Intervention
import Interventions
import Report
import Reports
import SystemClients
import SystemDispatcher
import SystemDispatchers
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class DaoMethods:DaoMethodsInterface {

    private fun resultRowToUser(row: ResultRow) = SystemClient(
        id = row[SystemClients.id],
        login = row[SystemClients.login],
        password = row[SystemClients.password],
        phone = row[SystemClients.phone],
        pesel = row[SystemClients.pesel],
        email = row[SystemClients.email],
        account_deleted = row[SystemClients.account_deleted]
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

    private fun resultRowToDispatcher(row: ResultRow) = SystemDispatcher(
        id = row[SystemDispatchers.id],
        name = row[SystemDispatchers.name],
        surname = row[SystemDispatchers.surname],
        phone = row[SystemDispatchers.phone],
        roleCode = row[SystemDispatchers.role]
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
            val insertStatement = SystemClients.insert {
                it[SystemClients.login] = login
                it[SystemClients.password] = password
                it[SystemClients.phone] = phone
                it[SystemClients.pesel] = pesel
                it[SystemClients.email] = email
                it[SystemClients.account_deleted] = false
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
            SystemClients.update({ SystemClients.id eq id }) {
                login?.let { firstName -> it[SystemClients.login] = firstName }
                password?.let { password -> it[SystemClients.password] = password }
                phone?.let { phone -> it[SystemClients.phone] = phone }
                pesel?.let { pesel -> it[SystemClients.pesel] = pesel }
                email?.let { email -> it[SystemClients.email] = email }
            } > 0
        }
    }

    override suspend fun deleteClient(id: Int): Boolean {
        return transaction {
            SystemClients.update({ SystemClients.id eq id }) {
                it[SystemClients.account_deleted] = true
            } > 0
        }
    }

    override suspend fun getClient(id: Int): SystemClient? {
        return transaction {
            SystemClients
                .selectAll().where { SystemClients.id eq id }
                .mapNotNull(::resultRowToUser)
                .singleOrNull()
        }
    }

    override suspend fun getAllClients(page: Int, pageSize: Int): List<SystemClient>{
        return transaction {
            val offset = (page - 1) * pageSize

            SystemClients.selectAll()
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

    override suspend fun addDispatcher(name: String, surname: String, phone: String, role: SystemDispatcher.Role): Boolean {
        return transaction {
            val insertStatement = SystemDispatchers.insert {
                it[SystemDispatchers.name] = name
                it[SystemDispatchers.surname] = surname
                it[SystemDispatchers.phone] = phone
                it[SystemDispatchers.role] = role.role.toShort()
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToDispatcher) != null
        }
    }

    override suspend fun deleteDispatcher(id: Int): Boolean {
        return transaction { SystemDispatchers.deleteWhere { SystemDispatchers.id eq id } > 0}
    }

    override suspend fun editDispatcher(id: Int, name: String?, surname: String?, phone: String?, role:SystemDispatcher.Role?): Boolean {
        return transaction {
            SystemDispatchers.update({ SystemDispatchers.id eq id }) {
                name?.let { name -> it[SystemDispatchers.name] = name }
                surname?.let { surname -> it[SystemDispatchers.surname] = surname }
                phone?.let { phone -> it[SystemDispatchers.phone] = phone }
                role?.let{role-> it[SystemDispatchers.role] = role.role.toShort()}
            } > 0
        }
    }

    override suspend fun getDispatcher(id: Int): SystemDispatcher? {
        return transaction {
            SystemDispatchers
                .selectAll().where { SystemDispatchers.id eq id }
                .mapNotNull(::resultRowToDispatcher)
                .singleOrNull()
        }
    }

    override suspend fun getAlDispatchers(page:Int, pageSize: Int): List<SystemDispatcher> {
        return transaction {
            val offset = (page - 1) * pageSize

            SystemDispatchers.selectAll()
                .limit(pageSize, offset.toLong())
                .map(::resultRowToDispatcher).toList()

        }
    }
}