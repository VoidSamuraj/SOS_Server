package dao

import Customer
import Employee
import Guard
import Intervention
import Report
import kotlinx.datetime.LocalDateTime

interface DaoMethodsInterface {

    //Client
    suspend fun addClient(login: String, password: String, phone: String, pesel: String, email: String):Boolean
    suspend fun editClient(id:Int, login: String?=null, password: String?=null, phone: String?=null, pesel: String?=null, email: String?=null):Boolean
    suspend fun deleteClient(id:Int):Boolean
    suspend fun getClient(id:Int):Customer?
    suspend fun getAllClients(page:Int, pageSize:Int):List<Customer>

    //Intervention
    suspend fun addIntervention(reportId: Int, guardId: Int, EmployeeId: Int, patrolNumber: Int):Boolean
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
    suspend fun addGuard(name:String, surname:String, phone: String):Boolean
    suspend fun editGuard(id:Int, name:String?=null, surname:String?=null, phone: String?=null):Boolean
    suspend fun deleteGuard(id:Int):Boolean
    suspend fun getGuard(id:Int):Guard?
    suspend fun getAllGuards(page:Int, pageSize: Int):List<Guard>

    //Employee
    suspend fun addEmployee(name: String,surname: String,password: String,phone: String, role:Employee.Role):Boolean
    suspend fun deleteEmployee(id:Int):Boolean
    suspend fun editEmployee(id:Int, name: String?=null,surname: String?=null,password: String?=null,phone: String?=null, role:Employee.Role?=null):Boolean
    suspend fun getEmployee(id:Int):Employee?
    suspend fun getAllEmployees(page:Int, pageSize: Int):List<Employee>
}