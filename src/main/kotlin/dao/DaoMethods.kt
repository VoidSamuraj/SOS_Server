package dao

import Customer
import CustomerInfo
import Guard
import Guards
import Intervention
import Interventions
import Report
import Reports
import Customers
import Employee
import EmployeeInfo
import Employees
import GuardInfo
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import plugins.notifyClientsAboutChanges
import security.HashPassword
import security.HashPassword.comparePasswords
import kotlin.collections.mapNotNull

object DaoMethods:DaoMethodsInterface {

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
        protection_expiration_date = row[Customers.protection_expiration_date]
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

    override suspend fun addCustomer(
        login: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        pesel: String,
        email: String,
        protectionExpirationDate: LocalDateTime?
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
                it[Customers.password] =  HashPassword.hashPassword(password)
                it[Customers.name] =  name
                it[Customers.surname] =  surname
                it[Customers.phone] = phone
                it[Customers.pesel] = pesel
                it[Customers.email] = email
                it[Customers.account_deleted] = false
                it[Customers.protection_expiration_date] = protectionExpirationDate
            }
            if(insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToClient) != null)
                return@transaction Triple(true, "Customer created successfully.", insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToClient(resultRow) })
            return@transaction Triple(false, "Failed to create client.", null)
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
    ): Pair<Boolean,String> {
        return transaction {

            val customerExists = Customers.selectAll().where { (Customers.id eq id) }.singleOrNull()
            if (customerExists == null) {
                return@transaction false to "Incorrect id for."
            }
            if (!comparePasswords(password, resultRowToClient(customerExists).password))
                return@transaction false to "Incorrect password."

            if (login != null) {
                val list = Customers.selectAll().where { Customers.login eq login }.mapNotNull(::resultRowToClient)
                if (list.count() > 0 && list.any { customer -> customer.id != id }) {
                    return@transaction Pair(false, "Login is already taken.")
                }
            }

            if (pesel != null) {
                val list = Customers.selectAll().where { Customers.pesel eq pesel }.mapNotNull(::resultRowToClient)
                if (list.count() > 0 && list.any { customer -> customer.id != id }) {
                    return@transaction Pair(false, "Pesel is already taken.")
                }
            }

            if (email != null) {
                val list = Customers.selectAll().where { Customers.email eq email }.mapNotNull(::resultRowToClient)
                if (list.count() > 0 && list.any { customer -> customer.id != id }) {
                    return@transaction Pair(false, "Email is already taken.")
                }
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
            if (result){
                notifyClientsAboutChanges("customers", id)
                return@transaction true to "Customer updated successfully."
            }
            return@transaction false to "Failed to update customer."
        }
    }

    override suspend fun editCustomer(id:Int, name: String?, surname: String?, phone: String?, pesel: String?, email: String?, isActive:Boolean?, protectionExpirationDate: LocalDateTime?): Pair<Boolean, String>{

        return transaction {

            val customerExists = Customers.selectAll().where{(Customers.id eq id)}.singleOrNull()
            if (customerExists == null) {
                return@transaction false to "Incorrect id for."
            }


            if(pesel != null){
                val list = Customers.selectAll().where{ Customers.pesel eq pesel}.mapNotNull(::resultRowToClient)
                if (list.count() > 0 && list.any {customer -> customer.id != id }) {
                    return@transaction Pair(false, "Pesel is already taken.")
                }
            }

            if(email != null){
                val list = Customers.selectAll().where{ Customers.email eq email}.mapNotNull(::resultRowToClient)
                if (list.count() > 0 && list.any {customer -> customer.id != id }) {
                    return@transaction Pair(false, "Email is already taken.")
                }
            }

            val result = Customers.update({ Customers.id eq id }) {
                name?.let { name -> it[Customers.name] = name }
                surname?.let { surname -> it[Customers.surname] = surname }
                phone?.let { phone -> it[Customers.phone] = phone }
                pesel?.let { pesel -> it[Customers.pesel] = pesel }
                email?.let { email -> it[Customers.email] = email }
                isActive?.let{isActive-> it[Customers.account_deleted] = !isActive}
                protectionExpirationDate?.let{protectionExpirationDate -> it[Customers.protection_expiration_date] = protectionExpirationDate}
            } > 0
            if(result) {
                notifyClientsAboutChanges("customers", id)
                return@transaction true to "Customer updated successfully."
            }
            return@transaction false to "Failed to update customer."
        }
    }
    override suspend fun deleteCustomer(id: Int): Boolean {
        return transaction {
            val ret =Customers.update({ Customers.id eq id }) {
                it[Customers.account_deleted] = true
            } > 0
            if(ret){
                notifyClientsAboutChanges("customers", id)
            }
            return@transaction ret
        }
    }

    override suspend fun restoreCustomer(id: Int): Boolean {
        return transaction {
            val ret = Customers.update({ Customers.id eq id }) {
                it[Customers.account_deleted] = false
            } > 0
            if(ret)
                notifyClientsAboutChanges("customers", id)
            return@transaction ret
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

    override suspend fun getCustomer(email: String): Customer? {
        return transaction {
            Customers
                .selectAll().where { Customers.email eq email }
                .mapNotNull(::resultRowToClient)
                .singleOrNull()
        }
    }

    override suspend fun getCustomer(login:String, password: String): Pair<String, Customer?> {
        return transaction {
            val customer = Customers
                .selectAll().where { (Customers.login eq login) }
                .mapNotNull(::resultRowToClient)
                .singleOrNull()
            if (customer == null) {
                return@transaction "Incorrect login." to null
            }

            if(!comparePasswords(password,customer.password))
                return@transaction  "Incorrect password." to null

            return@transaction "Success" to customer

        }
    }

    override suspend fun getCustomers(page:Int, pageSize: Int, filterColumn: Column<out Any>?, filterValue: String?, filterType:String?, sortBy: Column<out Any>?, sortDir: String?): List<CustomerInfo> {
        return transaction {
            val offset = (page - 1) * pageSize
            val query=Customers.selectAll().apply {
                if (filterColumn != null && filterValue !=null) {
                    try {
                        if (filterColumn.columnType is StringColumnType) {
                            val column = filterColumn as? Column<String>
                            if(column != null)
                                when (filterType) {
                                    "contains" -> where { column like "%$filterValue%" }
                                    "startsWith" -> where { column like "$filterValue%" }
                                    "endsWith" -> where { column like "%$filterValue" }
                                }
                        }else{
                            val column = filterColumn as? Column<Any>
                            if(column != null)
                                when (filterType) {
                                    "equals" -> where { column eq filterValue }
                                    "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                    "isNotEmpty" -> where { filterColumn.isNotNull() and ( column neq "") }
                                    "isAnyOf" -> where { column eq filterValue }
                                }
                        }
                    }catch(e:Error){
                        System.err.println("Error: ${e.message}")
                    }

                }
            }
            if (sortBy != null) {
                try{
                    query.orderBy(sortBy, if(sortDir=="desc") SortOrder.DESC else SortOrder.ASC)
                }catch(e:Error){
                    System.err.println("Error: ${e.message}")
                }
            }
            query.limit(pageSize, offset.toLong())
                .map(::resultRowToClientInfo).toList()

        }
    }

    override suspend fun getCustomers(ids: List<Int>):List<CustomerInfo>{
        return transaction {
            return@transaction Customers
                .selectAll().where { Customers.id inList ids }
                .mapNotNull(::resultRowToClientInfo)

        }
    }

    override suspend fun getAllCustomers(): List<CustomerInfo>{
        return transaction {
            Customers.selectAll()
                .map(::resultRowToClientInfo).toList()
        }
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Intervention Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun addIntervention(reportId: Int, guardId: Int, employeeId: Int, startTime: LocalDateTime, endTime: LocalDateTime, status:Intervention.InterventionStatus):Boolean {
        return transaction {
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
    }

    override suspend fun getIntervention(id: Int): Intervention? {
        return transaction {
            Interventions
                .selectAll().where { Interventions.id eq id }
                .mapNotNull(::resultRowToIntervention)
                .singleOrNull()
        }
    }

    override suspend fun getInterventions(page: Int, pageSize: Int, filterColumn: Column<out Any>?, filterValue: String?, filterType:String?, sortBy: Column<out Any>?, sortDir: String?): List<Intervention> {
        return transaction {
            val offset = (page - 1) * pageSize

            val query = Interventions.selectAll().apply {
                if (filterColumn != null && filterValue !=null) {
                    try{
                        if(filterColumn.columnType is StringColumnType){
                            val column = filterColumn as? Column<String>
                            if(column != null)
                                when (filterType) {
                                    "contains" -> where { column like "%$filterValue%" }
                                    "startsWith" -> where { column like "$filterValue%" }
                                    "endsWith" -> where { column like "%$filterValue" }
                                }
                        }else{
                            val column = filterColumn as? Column<Any>
                            if(column != null)
                                when (filterType) {
                                    "equals" -> where { column eq filterValue }
                                    "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                    "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                    "isAnyOf" -> where { column eq filterValue }
                                }
                        }
                    }catch(e:Error){
                        System.err.println("Error: ${e.message}")
                    }
                }
            }
            if (sortBy != null) {
                try{
                    query.orderBy(sortBy, if(sortDir=="desc") SortOrder.DESC else SortOrder.ASC)
                }catch(e:Error){
                    System.err.println("Error: ${e.message}")
                }
            }
            query.limit(pageSize, offset.toLong())
                .map(::resultRowToIntervention).toList()
        }
    }

    override suspend fun getAllInterventions(): List<Intervention> {
        return transaction {

            Interventions.selectAll()
                .map(::resultRowToIntervention).toList()
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


    override suspend fun getReports(page: Int, pageSize: Int, filterColumn: Column<out Any>?, filterValue: String?, filterType: String?, sortBy: Column<out Any>?, sortDir: String?): List<Report> {
        return transaction {
            val offset = (page - 1) * pageSize

            val query=Reports.selectAll().apply {
                if (filterColumn != null && filterValue !=null) {
                    try{
                        if(filterColumn.columnType is StringColumnType){
                            val column = filterColumn as? Column<String>
                            if(column != null)
                                when (filterType) {
                                    "contains" -> where { column like "%$filterValue%" }
                                    "startsWith" -> where { column like "$filterValue%" }
                                    "endsWith" -> where { column like "%$filterValue" }
                                }
                        }else{
                            val column = filterColumn as? Column<Any>
                            if(column != null)
                                when (filterType) {
                                    "equals" -> where { column eq filterValue }
                                    "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                    "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                    "isAnyOf" -> where { column eq filterValue }
                                }
                        }
                    }catch(e:Error){
                        System.err.println("Error: ${e.message}")
                    }
                }
            }
            if (sortBy != null) {
                try{
                    query.orderBy(sortBy, if(sortDir=="desc") SortOrder.DESC else SortOrder.ASC)
                }catch(e:Error){
                    System.err.println("Error: ${e.message}")
                }
            }
            query.limit(pageSize, offset.toLong())
                .map(::resultRowToReport).toList()

        }
    }

    override suspend fun getAllReports(filterFinished: Boolean): List<Report> {
        return transaction {
            Reports.selectAll().let { query ->
                if (filterFinished) {
                    query.where { (Reports.status eq 0) or (Reports.status eq 1) }
                } else {
                    query
                }
            }.map(::resultRowToReport).toList()
        }
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Guard Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun addGuard( login: String, password: String, name: String, surname: String, phone: String, email: String): Triple<Boolean, String, Guard?> {
        return transaction {

            if (Guards.select(Guards.login).where{ Guards.login eq login}.count() > 0) {
                return@transaction Triple(false, "Login is already taken.", null)
            }
            if (Guards.select(Guards.phone).where{ Guards.phone eq phone}.count() > 0) {
                return@transaction Triple(false, "Phone is already taken.", null)
            }

            val insertStatement = Guards.insert {
                it[Guards.login] = login
                it[Guards.password] =  HashPassword.hashPassword(password)
                it[Guards.name] = name
                it[Guards.surname] = surname
                it[Guards.phone] = phone
                it[Guards.email] = email
                it[Guards.account_deleted] = false
            }
            if(insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToGuard) != null)
                return@transaction Triple(true, "Guard created successfully.", insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToGuard(resultRow) })
            return@transaction Triple(false, "Failed to create guard.", null)
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
    ): Pair<Boolean, String> {
        return transaction {

            val guardExists = Guards.selectAll().where {(Guards.id eq id)}.singleOrNull()
            if (guardExists == null) {
                return@transaction false to "Incorrect Id for the Guard."
            }

            if(!comparePasswords(password,resultRowToGuard(guardExists).password))
                return@transaction  false to "Incorrect password for the guard."

            if(login != null){
                val list = Guards.selectAll().where{ Guards.login eq login}.mapNotNull(::resultRowToGuard)
                if (list.count() > 0 && list.any {guard -> guard.id != id }) {
                    return@transaction Pair(false, "Login is already taken.")
                }
            }
            if(phone != null){
                val list = Guards.selectAll().where{ Guards.phone eq phone}.mapNotNull(::resultRowToGuard)
                if (list.count() > 0 && list.any {guard -> guard.id != id }) {
                    return@transaction Pair(false, "Phone is already taken.")
                }
            }

            val updated = Guards.update({ Guards.id eq id }) {
                login?.let{ login -> it[Guards.login] = login}
                newPassword?.let{ newPassword -> it[Guards.password] = newPassword}
                name?.let { name -> it[Guards.name] = name }
                surname?.let { surname -> it[Guards.surname] = surname }
                phone?.let { phone -> it[Guards.phone] = phone }
                email?.let{email -> it[Guards.email] = email}
            } > 0

            if (updated) {
                notifyClientsAboutChanges("guards", id)
                return@transaction true to "Guard updated successfully."
            }
            return@transaction false to "Failed to update guard."
        }
    }
    override suspend fun editGuard(id:Int, name:String?, surname:String?, phone: String?, email: String?, isActive:Boolean?): Pair<Boolean, String>{
        return transaction {

            val guardExists = Guards.selectAll().where {(Guards.id eq id)}.singleOrNull()
            if (guardExists == null) {
                return@transaction false to "Incorrect Id for the Guard."
            }

            if(phone != null){
                val list = Guards.selectAll().where{ Guards.phone eq phone}.mapNotNull(::resultRowToGuard)
                if (list.count() > 0 && list.any {guard -> guard.id != id }) {
                    return@transaction Pair(false, "Phone is already taken.")
                }
            }

            val updated = Guards.update({ Guards.id eq id }) {
                name?.let { name -> it[Guards.name] = name }
                surname?.let { surname -> it[Guards.surname] = surname }
                phone?.let { phone -> it[Guards.phone] = phone }
                email?.let{email -> it[Guards.email] = email}
                isActive?.let{isActive-> it[Guards.account_deleted] = !isActive}
            } > 0

            if (updated) {
                notifyClientsAboutChanges("guards", id)
                return@transaction true to "Guard updated successfully."
            }
            return@transaction false to "Failed to update guard."
        }
    }

    override suspend fun changeGuardPassword(
        id: Int,
        password: String
    ): Pair<Boolean, String> {
        return transaction {

            val guardExists = Guards.selectAll().where{ (Guards.id eq id)}.singleOrNull()
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
    }

    override suspend fun deleteGuard(id: Int): Boolean {
        return transaction {
            val ret = Guards.update({ Guards.id eq id }) {
                it[Guards.account_deleted] = true
            } > 0
            if(ret)
                notifyClientsAboutChanges("guards", id)
            return@transaction ret
        }
    }

    override suspend fun restoreGuard(id: Int): Boolean {
        return transaction {
            val ret = Guards.update({ Guards.id eq id }) {
                it[Guards.account_deleted] = false
            } > 0
            if (ret)
                notifyClientsAboutChanges("guards", id)
            return@transaction ret
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

    override suspend fun getGuard(email: String): Guard? {
        return transaction {
            Guards
                .selectAll().where { Guards.email eq email }
                .mapNotNull(::resultRowToGuard)
                .singleOrNull()
        }
    }

    override suspend fun getGuard(login:String, password: String): Pair<String,Guard?> {
        return transaction {
            val guard =Guards
                .selectAll().where { (Guards.login eq login)}
                .mapNotNull(::resultRowToGuard)
                .singleOrNull()
            if(guard == null)
                return@transaction  "There is no guard with this Login." to null
            if(!comparePasswords(password,guard.password))
                return@transaction  "Incorrect password for the guard." to null
            return@transaction "Success" to guard
        }
    }

    override suspend fun getGuards(page:Int, pageSize: Int, filterColumn: Column<out Any>?, filterValue: String?, filterType:String?, sortBy: Column<out Any>?, sortDir: String?): List<GuardInfo> {
        return transaction {
            val offset = (page - 1) * pageSize
            val query=Guards.selectAll().apply {
                if (filterColumn != null && filterValue !=null) {
                    try{
                        if(filterColumn.columnType is StringColumnType){
                            val column = filterColumn as? Column<String>
                            if(column != null)
                                when (filterType) {
                                    "contains" -> where { column like "%$filterValue%" }
                                    "startsWith" -> where { column like "$filterValue%" }
                                    "endsWith" -> where { column like "%$filterValue" }
                                }
                        }else{
                            val column = filterColumn as? Column<Any>
                            if(column != null)
                                when (filterType) {
                                    "equals" -> where { column eq filterValue }
                                    "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                    "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                    "isAnyOf" -> where { column eq filterValue }
                                }
                        }
                    }catch(e:Error){
                        System.err.println("Error: ${e.message}")
                    }
                }
            }
            if (sortBy != null) {
                try{
                    query.orderBy(sortBy, if(sortDir=="desc") SortOrder.DESC else SortOrder.ASC)
                }catch(e:Error){
                    System.err.println("Error: ${e.message}")
                }
            }
            query.limit(pageSize, offset.toLong())
                .map(::resultRowToGuardInfo).toList()

        }
    }

    override suspend fun getGuards(ids: List<Int>): List<GuardInfo> {
        return transaction {
            return@transaction Guards
                .selectAll().where { Guards.id inList ids}
                .mapNotNull(::resultRowToGuardInfo)
        }
    }

    override suspend fun getAllGuards(): List<GuardInfo> {
        return transaction {

            Guards.selectAll()
                .map(::resultRowToGuardInfo).toList()
        }
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                    Employee Section
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun addEmployee(login: String, password: String, name: String, surname: String, phone: String, email:String, role: Employee.Role): Triple<Boolean,String,Employee?> {
        return transaction {

            if (Employees.select(Employees.login).where{ Employees.login eq login}.count() > 0) {
                return@transaction Triple(false, "Login is already taken.",null)
            }
            if (Employees.select(Employees.phone).where{ Employees.phone eq phone}.count() > 0) {
                return@transaction Triple(false, "Phone is already taken.",null)
            }
            if (Employees.select(Employees.email).where{ Employees.email eq email}.count() > 0) {
                return@transaction Triple(false, "Email is already taken.",null)
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
                return@transaction Triple(true, "Employee created successfully.",insertStatement.resultedValues?.firstOrNull()?.let { resultRow -> resultRowToEmployee(resultRow) })
            return@transaction Triple(false, "Failed to create employee.",null)
        }
    }

    override suspend fun editEmployee(id: Int, login: String?, password: String, newPassword: String?, name: String?, surname: String?, phone: String?, email: String?, role:Employee.Role?): Triple<Boolean, String, EmployeeInfo?> {
        return transaction {

            val employeeExists = Employees.selectAll().where{ (Employees.id eq id)}.singleOrNull()
            if (employeeExists == null) {
                return@transaction Triple(false, "Incorrect Id for the employee.", null)
            }

            if (login != null) {
                val list=  Employees.selectAll().where{ Employees.login eq login}.mapNotNull(::resultRowToEmployee)
                if(list.count() > 0 && list.any {employee -> employee.id != id })
                    return@transaction Triple(false, "Login is already taken.",null)
            }

            if (phone != null) {
                val list=  Employees.selectAll().where{ Employees.phone eq phone}.mapNotNull(::resultRowToEmployee)
                if(list.count() > 0 && list.any {employee -> employee.id != id })
                    return@transaction Triple(false, "Phone is already taken.",null)
            }
            if (email != null && Employees.select(Employees.email).where{ Employees.email eq email}.count() > 0) {
                val list=  Employees.selectAll().where{ Employees.email eq email}.mapNotNull(::resultRowToEmployee)
                if(list.count() > 0 && list.any {employee -> employee.id != id })
                    return@transaction Triple(false, "Email is already taken.",null)
            }

            if(!comparePasswords(password,resultRowToEmployee(employeeExists).password))
                return@transaction Triple(false, "Incorrect password for the employee.", null)

            val updated = Employees.update({ Employees.id eq id }) {
                login?.let { login -> it[Employees.login] = login }
                newPassword?.let { newPassword -> it[Employees.password] = HashPassword.hashPassword(newPassword) }
                name?.let { name -> it[Employees.name] = name }
                surname?.let { surname -> it[Employees.surname] = surname }
                phone?.let { phone -> it[Employees.phone] = phone }
                email?.let { email -> it[Employees.email] = email }
                role?.let{role-> it[Employees.role] = role.role.toShort()}
            } > 0
            if (updated) {
                notifyClientsAboutChanges("employees", id)
                val employeeInfo =  runBlocking{ getEmployee(id)?.toEmployeeInfo()}
                return@transaction Triple(true, "Employee updated successfully.", employeeInfo)
            }
            return@transaction Triple(false, "Failed to update employee.", null)
        }
    }
    override suspend fun editEmployee(id:Int, name: String?,surname: String?, phone: String?, email:String?, role:Employee.Role?, isActive:Boolean?): Pair<Boolean, String>{
        return transaction {

            val employeeExists = Employees.selectAll().where{ (Employees.id eq id)}.singleOrNull()
            if (employeeExists == null) {
                return@transaction false to "Incorrect Id for the employee."
            }


            if (phone != null) {
                val list=  Employees.selectAll().where{ Employees.phone eq phone}.mapNotNull(::resultRowToEmployee)
                if(list.count() > 0 && list.any {employee -> employee.id != id })
                    return@transaction Pair(false, "Phone is already taken.")
            }
            if (email != null && Employees.select(Employees.email).where{ Employees.email eq email}.count() > 0) {
                val list=  Employees.selectAll().where{ Employees.email eq email}.mapNotNull(::resultRowToEmployee)
                if(list.count() > 0 && list.any {employee -> employee.id != id })
                    return@transaction Pair(false, "Email is already taken.")
            }

            val updated = Employees.update({ Employees.id eq id }) {
                name?.let { name -> it[Employees.name] = name }
                surname?.let { surname -> it[Employees.surname] = surname }
                phone?.let { phone -> it[Employees.phone] = phone }
                email?.let { email -> it[Employees.email] = email }
                role?.let{role-> it[Employees.role] = role.role.toShort()}
                isActive?.let{isActive-> it[Employees.account_deleted] = !isActive}
            } > 0
            if (updated) {
                notifyClientsAboutChanges("employees", id)
                return@transaction true to "Employee updated successfully."
            }
            return@transaction false to "Failed to update employee."
        }
    }
    override suspend fun changeEmployeeRole(id: Int, role:Employee.Role): Pair<Boolean, String> {
        return transaction {

            val employeeExists = Employees.selectAll().where{ (Employees.id eq id)}.singleOrNull()
            if (employeeExists == null) {
                return@transaction false to "Incorrect Id for the employee."
            }

            val updated = Employees.update({ Employees.id eq id }) {
                it[Employees.role] = role.role.toShort()
            } > 0
            if (updated){
                notifyClientsAboutChanges("employees", id)
                return@transaction true to "Employee updated successfully."
            }
            return@transaction false to "Failed to update employee."
        }
    }
    override suspend fun changeEmployeePassword(id:Int, password:String): Pair<Boolean, String>{
        return transaction {

            val employeeExists = Employees.selectAll().where{ (Employees.id eq id)}.singleOrNull()
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
    }

    override suspend fun getEmployee(id: Int): Employee? {
        return transaction {
            Employees
                .selectAll().where { Employees.id eq id }
                .mapNotNull(::resultRowToEmployee)
                .singleOrNull()
        }
    }
    override suspend fun getEmployee(email: String): Employee? {
        return transaction {
            Employees
                .selectAll().where { Employees.email eq email }
                .mapNotNull(::resultRowToEmployee)
                .singleOrNull()
        }
    }
    override suspend fun getEmployee(login:String, password: String): Pair<String, Employee?> {
        return transaction {
            val employee = Employees
                .selectAll().where { (Employees.login eq login) }
                .mapNotNull(::resultRowToEmployee)
                .singleOrNull()
            if (employee == null) {
                return@transaction "Incorrect Id for the employee." to null
            }
            if(!comparePasswords(password,employee.password))
                return@transaction "Incorrect password for the employee." to null
            return@transaction "Success" to employee

        }
    }

    override suspend fun getEmployees(page:Int, pageSize: Int, filterColumn: Column<out Any>?, filterValue: String?, filterType:String?, sortBy: Column<out Any>?, sortDir: String?): List<EmployeeInfo> {
        return transaction {
            val offset = (page - 1) * pageSize
            val query=Employees.selectAll().apply {
                if (filterColumn != null && filterValue !=null) {
                    try{

                        if(filterColumn.columnType is StringColumnType){
                            val column = filterColumn as? Column<String>
                            if(column != null)
                                when (filterType) {
                                    "contains" -> where {column like "%$filterValue%" }
                                    "startsWith" -> where { column like "$filterValue%" }
                                    "endsWith" -> where { column like "%$filterValue" }
                                }
                        }else{
                            val column = filterColumn as? Column<Any>
                            if(column != null)
                                when (filterType) {
                                    "equals" -> where { column eq filterValue }
                                    "isEmpty" -> where { filterColumn.isNull() or (column eq "") }
                                    "isNotEmpty" -> where { filterColumn.isNotNull() and (column neq "") }
                                    "isAnyOf" -> where { column eq filterValue }
                                }
                        }
                    }catch(e:Error){
                        System.err.println("Error: ${e.message}")
                    }
                }
            }
            if (sortBy != null) {
                try{
                    query.orderBy(sortBy, if(sortDir=="desc") SortOrder.DESC else SortOrder.ASC)
                }catch(e:Error){
                    System.err.println("Error: ${e.message}")
                }
            }
            query.limit(pageSize, offset.toLong())
                .map(::resultRowToEmployeeInfo).toList()

        }
    }

    override suspend fun getEmployees(ids: List<Int>): List<EmployeeInfo> {
        return transaction {
            return@transaction Employees
                .selectAll().where { Employees.id inList ids}
                .mapNotNull(::resultRowToEmployeeInfo)
        }
    }

    override suspend fun getAllEmployees(): List<EmployeeInfo> {
        return transaction {

            Employees.selectAll()
                .map(::resultRowToEmployeeInfo).toList()

        }
    }

    override suspend fun deleteEmployee(id: Int): Boolean {
        return transaction {
            val ret =Employees.update({ Employees.id eq id }) {
                it[Employees.account_deleted] = true
            } > 0
            if(ret)
                notifyClientsAboutChanges("employees", id)
            return@transaction ret
        }
    }

    override suspend fun restoreEmployee(id: Int): Boolean {
        return transaction {
            val ret = Employees.update({ Employees.id eq id }) {
                it[Employees.account_deleted] = false
            } > 0
            if(ret)
                notifyClientsAboutChanges("employees", id)
            return@transaction ret
        }
    }
}
