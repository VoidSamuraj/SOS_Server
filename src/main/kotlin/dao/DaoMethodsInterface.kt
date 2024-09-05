package dao

import Customer
import Employee
import Guard
import Intervention
import Report
import kotlinx.datetime.LocalDateTime

interface DaoMethodsInterface {

    //Customer
    suspend fun addCustomer(login: String, password: String, phone: String, pesel: String, email: String): Triple<Boolean, String, Customer?>
    suspend fun editCustomer(id:Int, login: String?=null, password: String, newPassword: String?=null, phone: String?=null, pesel: String?=null, email: String?=null): Pair<Boolean, String>
    suspend fun deleteCustomer(id:Int):Boolean
    suspend fun getCustomer(id:Int):Customer?
    suspend fun getCustomer(login:String, password: String):Pair<String,Customer?>
    suspend fun getAllCustomers(page:Int, pageSize:Int):List<Customer>

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
    suspend fun addGuard(login: String, password: String, name:String, surname:String, phone: String): Triple<Boolean, String,Guard?>
    suspend fun editGuard(id:Int, login: String?=null, password: String, newPassword: String?=null, name:String?=null, surname:String?=null, phone: String?=null): Pair<Boolean, String>
    suspend fun deleteGuard(id:Int):Boolean
    suspend fun getGuard(id:Int):Guard?
    suspend fun getGuard(login:String, password: String):Pair<String,Guard?>
    suspend fun getAllGuards(page:Int, pageSize: Int):List<Guard>

    //Employee
    suspend fun deleteEmployee(id:Int):Boolean
    suspend fun editEmployee(id:Int, login: String?=null, password: String, newPassword: String?=null, name: String?=null,surname: String?=null, phone: String?=null, role:Employee.Role?=null): Pair<Boolean, String>
    suspend fun addEmployee(login: String, password: String, name: String,surname: String, phone: String, role:Employee.Role): Triple<Boolean,String,Employee?>
    suspend fun getEmployee(id:Int):Employee?
    suspend fun getEmployee(login:String, password: String):Pair<String,Employee?>
    suspend fun getAllEmployees(page:Int, pageSize: Int):List<Employee>
}