package dao

import SystemClient
import SystemDispatcher
import Guard
import Intervention
import Report
import kotlinx.datetime.LocalDateTime

interface DaoMethodsInterface {

    //Client
    suspend fun addClient(login: String, password: String, phone: String, pesel: String, email: String):Boolean
    suspend fun editClient(id:Int, login: String?=null, password: String?=null, phone: String?=null, pesel: String?=null, email: String?=null):Boolean
    suspend fun deleteClient(id:Int):Boolean
    suspend fun getClient(id:Int):SystemClient?
    suspend fun getAllClients(page:Int, pageSize:Int):List<SystemClient>

    //Intervention
    suspend fun addIntervention(reportId: Int, guardId: Int, dispatcherId: Int, patrolNumber: Int):Boolean
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

    //Dispatcher
    suspend fun addDispatcher(name: String,surname: String,password: String,phone: String, role:SystemDispatcher.Role):Boolean
    suspend fun deleteDispatcher(id:Int):Boolean
    suspend fun editDispatcher(id:Int, name: String?=null,surname: String?=null,password: String?=null,phone: String?=null, role:SystemDispatcher.Role?=null):Boolean
    suspend fun getDispatcher(id:Int):SystemDispatcher?
    suspend fun getAlDispatchers(page:Int, pageSize: Int):List<SystemDispatcher>
}