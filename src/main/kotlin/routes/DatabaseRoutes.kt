package routes

import io.ktor.server.routing.Route

fun Route.databaseRoutes() {








}



/*
    suspend fun addClient(login: String, password: String, phone: String, pesel: String, email: String):Pair<Boolean, String>
    suspend fun editClient(id:Int, login: String?=null, password: String, newPassword: String?=null, phone: String?=null, pesel: String?=null, email: String?=null):Pair<Boolean, String>
    suspend fun deleteClient(id:Int):Boolean
    suspend fun getClient(id:Int):Customer?
    suspend fun getClient(login:String, password: String):Customer?
    suspend fun getAllClients(page:Int, pageSize:Int):List<Customer>

    //Intervention
    suspend fun addIntervention(report_id: Int, guard_id: Int, employee_id: Int, start_time: LocalDateTime, end_time: LocalDateTime, status:Intervention.InterventionStatus, patrol_number: Int):Boolean
    suspend fun getIntervention(id:Int):Intervention?
    suspend fun getAllInterventions(page: Int, pageSize: Int):List<Intervention>

    //Report
    suspend fun addReport(clientId:Int, location:String, date: LocalDateTime, status:Report.ReportStatus):Boolean
    suspend fun deleteReport(id:Int):Boolean
    suspend fun updateReportLocation(id:Int, location: String):Boolean
    suspend fun changeReportStatus(id:Int, status:Report.ReportStatus):Boolean
    suspend fun getReport(id:Int):Report?
    suspend fun getAllReports(page:Int, pageSize: Int):List<Report>

    //Guard
    suspend fun addGuard(login: String, password: String, name:String, surname:String, phone: String):Pair<Boolean, String>
    suspend fun editGuard(id:Int, login: String?=null, password: String, newPassword: String?=null, name:String?=null, surname:String?=null, phone: String?=null):Pair<Boolean, String>
    suspend fun deleteGuard(id:Int):Boolean
    suspend fun getGuard(id:Int):Guard?
    suspend fun getGuard(login:String, password: String):Guard?
    suspend fun getAllGuards(page:Int, pageSize: Int):List<Guard>

    //Employee
    suspend fun deleteEmployee(id:Int):Boolean
    suspend fun editEmployee(id:Int, login: String?=null, password: String, newPassword: String?=null, name: String?=null,surname: String?=null, phone: String?=null, role:Employee.Role?=null):Pair<Boolean, String>
    suspend fun addEmployee(login: String, password: String, name: String,surname: String, phone: String, role:Employee.Role): Pair<Boolean,String>
    suspend fun getEmployee(id:Int):Employee?
    suspend fun getEmployee(login:String, password: String):Employee?
    suspend fun getAllEmployees(page:Int, pageSize: Int):List<Employee>
 */