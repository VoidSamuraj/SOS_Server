package routes

import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDateTime

fun Route.databaseRoutes() {

    //TODO check if authenticated
    route("/client"){
        post("/add"){
            val formParameters = call.receiveParameters()
            val login = formParameters["login"]
            val password = formParameters["password"]
            val phone = formParameters["phone"]
            val pesel = formParameters["pesel"]
            val email = formParameters["email"]

            if(login.isNullOrEmpty() || password.isNullOrEmpty() || phone.isNullOrEmpty() || pesel.isNullOrEmpty() || email.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.addCustomer(login.toString(), password.toString(), phone.toString(), pesel.toString(), email.toString())
            if(ret.first){
                call.respond(HttpStatusCode.OK,"Client added to database.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to add entry to the database. ${ret.second}")
            }
        }
        patch("/edit"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            val login = formParameters["login"]
            val password = formParameters["password"]
            val newPassword = formParameters["newPassword"]
            val phone = formParameters["phone"]
            val pesel = formParameters["pesel"]
            val email = formParameters["email"]

            if(id==null || password.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.editCustomer(id!!,login, password.toString(), newPassword, phone, pesel, email)
            if(ret.first){
                call.respond(HttpStatusCode.OK,"The client has been edited.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to edit client. ${ret.second}")
            }
        }
        get("/getById"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val customer = DaoMethods.getCustomer(id!!)
            if(customer!=null){
                call.respond(HttpStatusCode.OK,customer)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get client.")
            }
        }
        get("/getByCredentials"){
            val formParameters = call.receiveParameters()
            val login = formParameters["login"]
            val password = formParameters["password"]

            if(login.isNullOrEmpty() || password.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.getCustomer(login.toString(), password.toString())
            if(ret.second != null){
                call.respond(HttpStatusCode.OK, ret.second!!)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get client. ${ret.first}")
            }
        }
        get("/getPage"){
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            val customers = DaoMethods.getCustomers(page,size)
            call.respond(HttpStatusCode.OK,customers)
        }
        patch("/restore"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val restored = DaoMethods.restoreCustomer(id!!)
            if(restored)
                call.respond(HttpStatusCode.OK,"The client has been restored.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to restore client.")
        }
        delete{
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val deleted = DaoMethods.deleteCustomer(id!!)
            if(deleted)
                call.respond(HttpStatusCode.OK,"The client has been deleted.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to delete client.")
        }

    }


    route("/intervention"){
        post("/add"){
            val formParameters = call.receiveParameters()
            val report_id = formParameters["report_id"]?.toIntOrNull()
            val guard_id = formParameters["guard_id"]?.toIntOrNull()
            val employee_id = formParameters["employee_id"]?.toIntOrNull()
            val start_time = formParameters["start_time"]
            val end_time = formParameters["end_time"]
            val status = formParameters["id"]?.toIntOrNull()
            //TODO
            // val ret = DaoMethods.addIntervention(report_id, guard_id, employee_id, start_time, end_time,status)

        }

        get{
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
            val result = DaoMethods.getIntervention(id!!)
            if(result!=null)
                call.respond(HttpStatusCode.OK,result)
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to get intervention.")
        }
        get("/getPage"){
            val formParameters = call.receiveParameters()
            val page = formParameters["page"]
            val size = formParameters["size"]

            val customers = DaoMethods.getInterventions(page?.toIntOrNull()?:1,size?.toIntOrNull()?:10)
            call.respond(HttpStatusCode.OK,customers)
        }

    }


    route("/report"){
        post("/add"){
            val formParameters = call.receiveParameters()
            val client_id = formParameters["client_id"]?.toIntOrNull()
            val location = formParameters["location"]
            val date = formParameters["date"]
            val status = formParameters["status"]?.toIntOrNull()

            if(client_id==null || location.isNullOrEmpty() || date.isNullOrEmpty() || status == null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
            val result = DaoMethods.addReport(client_id!!, location.toString(), LocalDateTime.parse(date.toString()), Report.ReportStatus.fromInt(status!!))
            if(result){
                call.respond(HttpStatusCode.OK,"Report added to database.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to add report to the database.")
            }
        }
        patch("/updateLocation"){

            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            val location = formParameters["location"]

            if(id==null || location.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.updateReportLocation(id!!,location.toString())
            if(ret){
                call.respond(HttpStatusCode.OK,"The report's location has been edited.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to edit report's location.")
            }

        }
        patch("/changeReportStatus"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            val status = formParameters["status"]?.toIntOrNull()

            if(id==null || status==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.changeReportStatus(id!!, Report.ReportStatus.fromInt(status!!))
            if(ret){
                call.respond(HttpStatusCode.OK,"The report's status has been edited.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to edit report's status.")
            }
        }

        get{
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val report = DaoMethods.getReport(id!!)
            if(report!=null){
                call.respond(HttpStatusCode.OK,report)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get report.")
            }
        }
        get("/getPage"){
            val formParameters = call.receiveParameters()
            val page = formParameters["page"]
            val size = formParameters["size"]

            val reports = DaoMethods.getReports(page?.toIntOrNull()?:1,size?.toIntOrNull()?:10)
            call.respond(HttpStatusCode.OK,reports)
        }

        delete{
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
            val deleted = DaoMethods.deleteReport(id!!)
            if(deleted)
                call.respond(HttpStatusCode.OK,"The report has been deleted.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to delete report.")
        }

    }


    route("/guard"){
        post("/add"){
            val formParameters = call.receiveParameters()
            val login = formParameters["login"]
            val password = formParameters["password"]
            val phone = formParameters["phone"]
            val name = formParameters["name"]
            val surname = formParameters["surname"]

            if(login.isNullOrEmpty() || password.isNullOrEmpty() || phone.isNullOrEmpty() || name.isNullOrEmpty() || surname.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.addGuard(login.toString(), password.toString(), name.toString(), surname.toString(), phone.toString())
            if(ret.first){
                call.respond(HttpStatusCode.OK,"Guard added to database.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to add entry to the database. ${ret.second}")
            }


        }
        patch("/edit"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            val login = formParameters["login"]
            val password = formParameters["password"]
            val newPassword = formParameters["newPassword"]
            val phone = formParameters["phone"]
            val name = formParameters["name"]
            val surname = formParameters["surname"]

            if(id==null || password.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.editGuard(id!!,login, password.toString(), newPassword, name, surname, phone)
            if(ret.first){
                call.respond(HttpStatusCode.OK,"The guard has been edited.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to edit guard. ${ret.second}")
            }
        }
        get("/getById"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val guard = DaoMethods.getGuard(id!!)
            if(guard!=null){
                call.respond(HttpStatusCode.OK,guard)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get guard.")
            }
        }
        get("/getByCredentials"){
            val formParameters = call.receiveParameters()
            val login = formParameters["login"]
            val password = formParameters["password"]

            if(login.isNullOrEmpty() || password.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.getGuard(login.toString(), password.toString())
            if(ret.second != null){
                call.respond(HttpStatusCode.OK, ret.second!!)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get guard. ${ret.first}")
            }
        }
        get("/getPage"){
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            val guards = DaoMethods.getGuards(page,size)
            call.respond(HttpStatusCode.OK,guards)
        }
        get("/getAll"){
            val guards = DaoMethods.getAllGuards()
            call.respond(HttpStatusCode.OK,guards)
        }

        patch("/restore"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val restored = DaoMethods.restoreGuard(id!!)
            if(restored)
                call.respond(HttpStatusCode.OK,"The guard has been restored.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to restore guard.")
        }
        delete{
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val deleted = DaoMethods.deleteGuard(id!!)
            if(deleted)
                call.respond(HttpStatusCode.OK,"The guard has been deleted.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to delete guard.")
        }

    }
    route("/employee"){
        post("/add"){
            val formParameters = call.receiveParameters()
            val login = formParameters["login"]
            val password = formParameters["password"]
            val phone = formParameters["phone"]
            val email = formParameters["email"]
            val name = formParameters["name"]
            val surname = formParameters["surname"]
            val roleCode = formParameters["roleCode"]?.toIntOrNull()

            if(login.isNullOrEmpty() || password.isNullOrEmpty() || phone.isNullOrEmpty() || name.isNullOrEmpty() || surname.isNullOrEmpty() ||  roleCode == null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.addEmployee(login.toString(), password.toString(), name.toString(), surname.toString(), phone.toString(), email.toString(), Employee.Role.fromInt(roleCode!!))
            if(ret.first){
                call.respond(HttpStatusCode.OK,"Employee added to database.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to add entry to the database. ${ret.second}")
            }
        }
        patch("/changeRole"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            val roleCode = formParameters["roleCode"]?.toIntOrNull()
            val role = if (roleCode == null) null else Employee.Role.fromInt(roleCode)

            if(id==null || role == null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
            val ret = DaoMethods.changeEmployeeRole(id!!, role!!)
            if(ret.first){
                call.respond(HttpStatusCode.OK,"The employee has been edited.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to edit employee. ${ret.second}")
            }
        }
        patch("/edit"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()
            val login = formParameters["login"]
            val password = formParameters["password"]
            val newPassword = formParameters["newPassword"]
            val phone = formParameters["phone"]
            val email = formParameters["email"]
            val name = formParameters["name"]
            val surname = formParameters["surname"]
            val roleCode = formParameters["roleCode"]?.toIntOrNull()
            val role = if (roleCode == null) null else Employee.Role.fromInt(roleCode)

            if(id==null || password.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val ret = DaoMethods.editEmployee(id!!,login, password.toString(), newPassword, name, surname, phone,email, role)
            if(ret.first){
                call.respond(HttpStatusCode.OK,"The employee has been edited.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to edit employee. ${ret.second}")
            }
        }
        get("/getById"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val employee = DaoMethods.getEmployee(id!!)
            if(employee!=null){
                call.respond(HttpStatusCode.OK,employee)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get employee.")
            }
        }
        //TODO
        get("/getByCredentials"){
            val formParameters = call.receiveParameters()
            val login = formParameters["login"]
            val password = formParameters["password"]

            if(login.isNullOrEmpty() || password.isNullOrEmpty())
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
            val ret = DaoMethods.getCustomer(login.toString(), password.toString())
            if(ret.second != null){
                call.respond(HttpStatusCode.OK, ret.second!!)
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to get employee. ${ret.first}")
            }
        }
        get("/getPage"){
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            val employees = DaoMethods.getEmployees(page,size)
            call.respond(HttpStatusCode.OK,employees)
        }
        get("/getAll"){
            val employees = DaoMethods.getAllEmployees()
            call.respond(HttpStatusCode.OK,employees)
        }

        patch("/restore"){
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val restored = DaoMethods.restoreEmployee(id!!)
            if(restored)
                call.respond(HttpStatusCode.OK,"The employee has been restored.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to restore employee.")
        }
        delete{
            val formParameters = call.receiveParameters()
            val id = formParameters["id"]?.toIntOrNull()

            if(id==null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")

            val deleted = DaoMethods.deleteEmployee(id!!)
            if(deleted)
                call.respond(HttpStatusCode.OK,"The employee has been deleted.")
            else
                call.respond(HttpStatusCode.InternalServerError, "Failed to delete employee.")
        }

    }

}



