package routes

import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail
import kotlinx.datetime.LocalDateTime
import plugins.isPeselValid
import plugins.sanitizeHtml
import plugins.isEmailValid
import plugins.isLoginValid
import plugins.isPasswordValid
import plugins.isPhoneValid
import plugins.isUsernameValid

// Maps used for sort selection
val GuardField = mapOf(
    "id" to Guards.id ,
    "name" to Guards.name,
    "surname" to Guards.surname,
    "phone" to Guards.phone,
    "account_active" to Guards.account_deleted
)
val CustomerField = mapOf(
    "id" to Customers.id,
    "phone" to Customers.phone,
    "pesel" to Customers.pesel,
    "email" to Customers.email,
    "account_active" to Customers.account_deleted
)
val EmployeeField = mapOf(
    "id" to Employees.id,
    "name" to Employees.name,
    "surname" to Employees.surname,
    "phone" to Employees.phone,
    "role" to Employees.role,
    "account_active" to Employees.account_deleted
)
private val ReportField = mapOf(
    "id" to Reports.id,
    "client_id" to Reports.client_id,
    "location" to Reports.location,
    "date" to Reports.date,
    "status" to Reports.status
)
private val InterventionField = mapOf(
    "id" to Interventions.id,
    "report_id" to Interventions.report_id,
    "guard_id" to Interventions.guard_id,
    "employee_id" to Interventions.employee_id,
    "start_time" to Interventions.start_time,
    "end_time" to Interventions.end_time,
    "status" to Interventions.status
)
fun Route.databaseRoutes() {

    route("/client"){
        intercept(ApplicationCallPipeline.Plugins) {
            checkUserPermission(onSuccess = {
                proceed()
            }, onFailure = {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to perform this action.")
                finish()
            })
        }

        post("/add"){
            try {
                val formParameters = call.receiveParameters()
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val pesel = sanitizeHtml(formParameters.getOrFail("pesel"))
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                val protectionExpirationDate = formParameters["protection_expiration_date"]?.let { sanitizeHtml(it) }
                val protectionExpirationDateTime = protectionExpirationDate?.let { LocalDateTime.parse(it.toString()) }

                if (login.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty() || phone.isEmpty() || pesel.isEmpty() || email.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                if(!isLoginValid(login)){
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@post
                }
                if(!isPasswordValid(password)){
                    call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                    return@post
                }
                if(!isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@post
                }
                if(!isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
                if(!isPeselValid(pesel)){
                    call.respond(HttpStatusCode.BadRequest, "Pesel is in wrong format")
                    return@post
                }
                if(!isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@post
                }
                if(!isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@post
                }
                val ret = DaoMethods.addCustomer(
                    login.toString(),
                    password.toString(),
                    name.toString(),
                    surname.toString(),
                    phone.toString(),
                    pesel.toString(),
                    email.toString(),
                    protectionExpirationDateTime
                )
                if (ret.first) {
                    call.respond(HttpStatusCode.OK, "Client added to database.")
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to add entry to the database. ${ret.second}"
                    )
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to add entry to the database. ${e.message}"
                )
            }

        }
        patch("/edit"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val login = formParameters["login"]?.let { sanitizeHtml(it) }
                val password = formParameters["password"]?.let { sanitizeHtml(it) }
                val newPassword = formParameters["newPassword"]?.let { sanitizeHtml(it) }
                val name = formParameters["name"]?.let { sanitizeHtml(it) }
                val surname = formParameters["surname"]?.let { sanitizeHtml(it) }
                val phone = formParameters["phone"]?.let { sanitizeHtml(it) }
                val pesel = formParameters["pesel"]?.let { sanitizeHtml(it) }
                val email = formParameters["email"]?.let { sanitizeHtml(it) }
                val protectionExpirationDate = formParameters["protection_expiration_date"]?.let { sanitizeHtml(it) }
                val protectionExpirationDateTime = protectionExpirationDate?.let { LocalDateTime.parse(it.toString()) }

                if(id==null || password.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                if(!login.isNullOrEmpty() && !isLoginValid(login)){
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@patch
                }
                if(!phone.isNullOrEmpty() && !isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@patch
                }
                if(!newPassword.isNullOrEmpty() && !isPasswordValid(newPassword)){
                    call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                    return@patch
                }
                if(!email.isNullOrEmpty() && !isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@patch
                }
                if(!pesel.isNullOrEmpty() && !isPeselValid(pesel)){
                    call.respond(HttpStatusCode.BadRequest, "Pesel is in wrong format")
                    return@patch
                }
                if(!name.isNullOrEmpty() && !isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@patch
                }
                if(!surname.isNullOrEmpty() && !isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@patch
                }
                val ret = DaoMethods.editCustomer(id,login, password.toString(), newPassword, name, surname, phone, pesel, email, protectionExpirationDateTime)
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"The client has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit client. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit client. ${e.message}"
                )
            }
        }

        patch("/editSudo"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val name = formParameters["name"]?.let { sanitizeHtml(it) }
                val surname = formParameters["surname"]?.let { sanitizeHtml(it) }
                val phone = formParameters["phone"]?.let { sanitizeHtml(it) }
                val pesel = formParameters["pesel"]?.let { sanitizeHtml(it) }
                val email = formParameters["email"]?.let { sanitizeHtml(it) }
                val isActive = formParameters["isActive"].toBoolean()
                val protectionExpirationDate = formParameters["protection_expiration_date"]?.let { sanitizeHtml(it) }
                val protectionExpirationDateTime = protectionExpirationDate?.let { LocalDateTime.parse(it.toString()) }

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                if(!phone.isNullOrEmpty() && !isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@patch
                }
                if(!email.isNullOrEmpty() && !isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@patch
                }
                if(!name.isNullOrEmpty() && !isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@patch
                }
                if(!surname.isNullOrEmpty() && !isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@patch
                }
                val ret = DaoMethods.editCustomer(id, name, surname, phone, pesel, email, isActive, protectionExpirationDateTime)
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"The client has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit client. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit client. ${e.message}"
                )
            }
        }
        get("/getById"){
            try{
                val id = call.request.queryParameters["id"]?.toIntOrNull()
                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }

                val customer = DaoMethods.getCustomer(id)
                if(customer!=null){
                    call.respond(HttpStatusCode.OK,customer)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get client.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get client. ${e.message}"
                )
            }
        }
        get("/getByCredentials"){
            try{
                val login = call.request.queryParameters["login"]?.let { sanitizeHtml(it) }
                val password = call.request.queryParameters["password"]?.let { sanitizeHtml(it) }

                if(login.isNullOrEmpty() || password.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }

                val ret = DaoMethods.getCustomer(login.toString(), password.toString())
                if(ret.second != null){
                    call.respond(HttpStatusCode.OK, ret.second!!)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get client. ${ret.first}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get client. ${e.message}"
                )
            }
        }
        get("/getPage"){
            try{
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val filterColumn = call.request.queryParameters["filterColumn"]?.let { sanitizeHtml(it) }
                val filterValue = call.request.queryParameters["filterValue"]?.let { sanitizeHtml(it) }
                val filterType = call.request.queryParameters["filterType"]?.let { sanitizeHtml(it) }
                val sortColumn = call.request.queryParameters["sortColumn"]?.let { sanitizeHtml(it) }
                val sortDir = call.request.queryParameters["sortDir"]?.let { sanitizeHtml(it) }
                val customers = DaoMethods.getCustomers(page,size, CustomerField[filterColumn], filterValue,filterType, CustomerField[sortColumn],sortDir)
                call.respond(HttpStatusCode.OK,customers)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get clients. ${e.message}"
                )
            }
        }
        patch("/restore"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }

                val restored = DaoMethods.restoreCustomer(id)
                if(restored)
                    call.respond(HttpStatusCode.OK,"The client has been restored.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to restore client.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to restore client. ${e.message}"
                )
            }
        }
        delete{
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@delete
                }

                val deleted = DaoMethods.deleteCustomer(id)
                if(deleted)
                    call.respond(HttpStatusCode.OK,"The client has been deleted.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete client.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to delete client. ${e.message}"
                )
            }
        }

    }


    route("/intervention"){
        intercept(ApplicationCallPipeline.Plugins) {
            checkUserPermission(onSuccess = {
                proceed()
            }, onFailure = {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to perform this action.")
                finish()
            })
        }

        post("/add"){
            try{
                val formParameters = call.receiveParameters()
                val reportId = formParameters["report_id"]?.toIntOrNull()
                val guardId = formParameters["guard_id"]?.toIntOrNull()
                val employeeId = formParameters["employee_id"]?.toIntOrNull()
                val startTime = formParameters["start_time"]?.let { sanitizeHtml(it) }
                val endTime = formParameters["end_time"]?.let { sanitizeHtml(it) }
                val status = formParameters["id"]?.toIntOrNull()
                if(reportId == null || guardId == null ||employeeId == null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }

                val ret = DaoMethods.addIntervention(reportId,
                    guardId,
                    employeeId, LocalDateTime.parse(startTime.toString()), LocalDateTime.parse(endTime.toString()), Intervention.InterventionStatus.fromInt(status!!))
                if(ret){
                    call.respond(HttpStatusCode.OK,"Intervention added to database.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add entry to the database.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to add intervention. ${e.message}"
                )
            }
        }

        get{
            try{
                val id = call.request.queryParameters["id"]?.toIntOrNull()
                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }
                val result = DaoMethods.getIntervention(id)
                if(result!=null)
                    call.respond(HttpStatusCode.OK,result)
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get intervention.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get intervention. ${e.message}"
                )
            }
        }
        get("/getPage"){
            try{
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val filterColumn = call.request.queryParameters["filterColumn"]?.let { sanitizeHtml(it) }
                val filterValue = call.request.queryParameters["filterValue"]?.let { sanitizeHtml(it) }
                val filterType = call.request.queryParameters["filterType"]?.let { sanitizeHtml(it) }
                val sortColumn = call.request.queryParameters["sortColumn"]?.let { sanitizeHtml(it) }
                val sortDir = call.request.queryParameters["sortDir"]?.let { sanitizeHtml(it) }

                val customers = DaoMethods.getInterventions(page,size, InterventionField[filterColumn], filterValue,filterType, InterventionField[sortColumn],sortDir)
                call.respond(HttpStatusCode.OK,customers)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get intervention. ${e.message}"
                )
            }
        }

    }


    route("/report"){
        intercept(ApplicationCallPipeline.Plugins) {
            checkUserPermission(onSuccess = {
                proceed()
            }, onFailure = {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to perform this action.")
                finish()
            })
        }
        post("/add"){
            try{
                val formParameters = call.receiveParameters()
                val clientId = formParameters["client_id"]?.toIntOrNull()
                val location = formParameters["location"]?.let { sanitizeHtml(it) }
                val date = formParameters["date"]?.let { sanitizeHtml(it) }
                val status = formParameters["status"]?.toIntOrNull()

                if(clientId==null || location.isNullOrEmpty() || date.isNullOrEmpty() || status == null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                val result = DaoMethods.addReport(
                    clientId, location.toString(), LocalDateTime.parse(date.toString()), Report.ReportStatus.fromInt(
                        status
                    ))
                if(result){
                    call.respond(HttpStatusCode.OK,"Report added to database.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add report to the database.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to add report to the database. ${e.message}"
                )
            }
        }
        patch("/updateLocation"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val location = formParameters["location"]?.let { sanitizeHtml(it) }

                if(id==null || location.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }

                val ret = DaoMethods.updateReportLocation(id,location.toString())
                if(ret){
                    call.respond(HttpStatusCode.OK,"The report's location has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit report's location.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit report's location. ${e.message}"
                )
            }
        }
        patch("/changeReportStatus"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val status = formParameters["status"]?.toIntOrNull()

                if(id==null || status==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }

                val ret = DaoMethods.changeReportStatus(id, Report.ReportStatus.fromInt(status))
                if(ret){
                    call.respond(HttpStatusCode.OK,"The report's status has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit report's status.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit report's status. ${e.message}"
                )
            }
        }

        get{
            try{
                val id = call.request.queryParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }

                val report = DaoMethods.getReport(id)
                if(report!=null){
                    call.respond(HttpStatusCode.OK,report)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get report.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get report. ${e.message}"
                )
            }
        }
        get("/getPage"){
            try{
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val filterColumn = call.request.queryParameters["filterColumn"]?.let { sanitizeHtml(it) }
                val filterValue = call.request.queryParameters["filterValue"]?.let { sanitizeHtml(it) }
                val filterType = call.request.queryParameters["filterType"]?.let { sanitizeHtml(it) }
                val sortColumn = call.request.queryParameters["sortColumn"]?.let { sanitizeHtml(it) }
                val sortDir = call.request.queryParameters["sortDir"]?.let { sanitizeHtml(it) }

                val reports = DaoMethods.getReports(page,size, ReportField[filterColumn], filterValue,filterType, ReportField[sortColumn],sortDir)
                call.respond(HttpStatusCode.OK,reports)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get reports. ${e.message}"
                )
            }
        }

        get("/getAll"){
            try{
                val customers = DaoMethods.getAllReports(false)
                call.respond(HttpStatusCode.OK,customers)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get intervention. ${e.message}"
                )
            }
        }
        delete{
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@delete
                }
                val deleted = DaoMethods.deleteReport(id)
                if(deleted)
                    call.respond(HttpStatusCode.OK,"The report has been deleted.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete report.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to delete report. ${e.message}"
                )
            }
        }

    }


    route("/guard"){
        intercept(ApplicationCallPipeline.Plugins) {
            checkUserPermission(onSuccess = {
                proceed()
            }, onFailure = {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to perform this action.")
                finish()
            })
        }

        post("/add"){
            try{
                val formParameters = call.receiveParameters()
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val email =  sanitizeHtml(formParameters.getOrFail("email"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))

                if(login.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty() || name.isEmpty() || surname.isEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                if(!isLoginValid(login)){
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@post
                }
                if(!isPasswordValid(password)){
                    call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                    return@post
                }
                if(!isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@post
                }
                if(!isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
                if(!isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@post
                }
                if(!isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@post
                }
                val ret = DaoMethods.addGuard(login.toString(), password.toString(), name.toString(), surname.toString(), phone.toString(), email.toString())
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"Guard added to database.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add entry to the database. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to add guard. ${e.message}"
                )
            }

        }
        patch("/edit"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val login = formParameters["login"]?.let { sanitizeHtml(it) }
                val password = formParameters["password"]?.let { sanitizeHtml(it) }
                val newPassword = formParameters["newPassword"]?.let { sanitizeHtml(it) }
                val phone = formParameters["phone"]?.let { sanitizeHtml(it) }
                val email =  formParameters["email"]?.let { sanitizeHtml(it) }
                val name = formParameters["name"]?.let { sanitizeHtml(it) }
                val surname = formParameters["surname"]?.let { sanitizeHtml(it) }

                if(id==null || password.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                if(!login.isNullOrEmpty() && !isLoginValid(login)){
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@patch
                }
                if(!newPassword.isNullOrEmpty() && !isPasswordValid(newPassword)){
                    call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                    return@patch
                }
                if(!phone.isNullOrEmpty() && !isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@patch
                }
                if(!email.isNullOrEmpty() && !isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@patch
                }
                if(!name.isNullOrEmpty() && !isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@patch
                }
                if(!surname.isNullOrEmpty() && !isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@patch
                }
                val ret = DaoMethods.editGuard(id,login, password.toString(), newPassword, name, surname, phone, email)
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"The guard has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit guard. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit guard. ${e.message}"
                )
            }
        }
        patch("/editSudo"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val name = formParameters["name"]?.let { sanitizeHtml(it) }
                val surname = formParameters["surname"]?.let { sanitizeHtml(it) }
                val phone = formParameters["phone"]?.let { sanitizeHtml(it) }
                val email =  formParameters["email"]?.let { sanitizeHtml(it) }
                val isActive = formParameters["isActive"].toBoolean()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                if(!phone.isNullOrEmpty() && !isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@patch
                }
                if(!email.isNullOrEmpty() && !isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@patch
                }
                if(!name.isNullOrEmpty() && !isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@patch
                }
                if(!surname.isNullOrEmpty() && !isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@patch
                }
                val ret = DaoMethods.editGuard(id, name = name, surname = surname, phone = phone, email = email, isActive = isActive)
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"The guard has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit guard. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit guard. ${e.message}"
                )
            }
        }
        get("/getById"){
            try{
                val id = call.request.queryParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }

                val guard = DaoMethods.getGuard(id)
                if(guard!=null){
                    call.respond(HttpStatusCode.OK,guard)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get guard.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get guard. ${e.message}"
                )
            }
        }
        get("/getByCredentials"){
            try{
                val login = call.request.queryParameters["login"]?.let { sanitizeHtml(it) }
                val password = call.request.queryParameters["password"]?.let { sanitizeHtml(it) }

                if(login.isNullOrEmpty() || password.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }

                val ret = DaoMethods.getGuard(login.toString(), password.toString())
                if(ret.second != null){
                    call.respond(HttpStatusCode.OK, ret.second!!)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get guard. ${ret.first}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get guard. ${e.message}"
                )
            }
        }
        get("/getPage"){
            try{
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val filterColumn = call.request.queryParameters["filterColumn"]?.let { sanitizeHtml(it) }
                val filterValue = call.request.queryParameters["filterValue"]?.let { sanitizeHtml(it) }
                val filterType = call.request.queryParameters["filterType"]?.let { sanitizeHtml(it) }
                val sortColumn = call.request.queryParameters["sortColumn"]?.let { sanitizeHtml(it) }
                val sortDir = call.request.queryParameters["sortDir"]?.let { sanitizeHtml(it) }
                val guards = DaoMethods.getGuards(page,size, GuardField[filterColumn], filterValue,filterType, GuardField[sortColumn],sortDir)
                call.respond(HttpStatusCode.OK,guards)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get guards. ${e.message}"
                )
            }
        }
        get("/getAll"){
            try{
                val guards = DaoMethods.getAllGuards()
                call.respond(HttpStatusCode.OK,guards)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get guards. ${e.message}"
                )
            }
        }

        patch("/restore"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }

                val restored = DaoMethods.restoreGuard(id)
                if(restored)
                    call.respond(HttpStatusCode.OK,"The guard has been restored.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to restore guard.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to restore guard. ${e.message}"
                )
            }
        }
        delete{
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@delete
                }

                val deleted = DaoMethods.deleteGuard(id)
                if(deleted)
                    call.respond(HttpStatusCode.OK,"The guard has been deleted.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete guard.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to delete guard. ${e.message}"
                )
            }
        }


    }
    route("/employee"){
        intercept(ApplicationCallPipeline.Plugins) {
            checkUserPermission(onSuccess = {
                proceed()
            }, onFailure = {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to perform this action.")
                finish()
            })
        }

        post("/add"){
            try {
                val formParameters = call.receiveParameters()
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))
                val roleCode = formParameters["roleCode"]?.toIntOrNull()

                if (login.isEmpty() || password.isEmpty() || phone.isEmpty() || name.isEmpty() || surname.isEmpty() || roleCode == null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                if(!isLoginValid(login)){
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@post
                }
                if(!isPasswordValid(password)){
                    call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                    return@post
                }
                if(!isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@post
                }
                if(!isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
                if(!isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@post
                }
                if(!isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@post
                }
                val ret = DaoMethods.addEmployee(
                    login.toString(),
                    password.toString(),
                    name.toString(),
                    surname.toString(),
                    phone.toString(),
                    email.toString(),
                    Employee.Role.fromInt(roleCode)
                )
                if (ret.first) {
                    call.respond(HttpStatusCode.OK, "Employee added to database.")
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to add entry to the database. ${ret.second}"
                    )
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to add employee. ${e.message}"
                )
            }
        }

        patch("/changeRole"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val roleCode = formParameters["roleCode"]?.toIntOrNull()
                val role = if (roleCode == null) null else Employee.Role.fromInt(roleCode)

                if(id==null || role == null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                val ret = DaoMethods.changeEmployeeRole(id, role)
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"The employee has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit employee. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit employee. ${e.message}"
                )
            }
        }
        patch("/edit"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val login = formParameters["login"]?.let { sanitizeHtml(it) }
                val password = formParameters["password"]?.let { sanitizeHtml(it) }
                val newPassword = formParameters["newPassword"]?.let { sanitizeHtml(it) }
                val phone = formParameters["phone"]?.let { sanitizeHtml(it) }
                val email = formParameters["email"]?.let { sanitizeHtml(it) }
                val name = formParameters["name"]?.let { sanitizeHtml(it) }
                val surname = formParameters["surname"]?.let { sanitizeHtml(it) }
                val roleCode = formParameters["roleCode"]?.toIntOrNull()
                val role = if (roleCode == null) null else Employee.Role.fromInt(roleCode)

                if(id==null || password.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                if(!login.isNullOrEmpty() && !isLoginValid(login)){
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@patch
                }
                if(!newPassword.isNullOrEmpty() && !isPasswordValid(newPassword)){
                    call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                    return@patch
                }
                if(!phone.isNullOrEmpty() && !isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@patch
                }
                if(!email.isNullOrEmpty() && !isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@patch
                }
                if(!name.isNullOrEmpty() && !isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@patch
                }
                if(!surname.isNullOrEmpty() && !isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@patch
                }
                val ret = DaoMethods.editEmployee(id,login, password.toString(), newPassword, name, surname, phone,email, role)
                if(ret.first && ret.third!=null){
                    call.respond(HttpStatusCode.OK,ret.third!!)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit employee. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit employee. ${e.message}"
                )
            }
        }

        patch("/editSudo"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()
                val phone = formParameters["phone"]?.let { sanitizeHtml(it) }
                val email = formParameters["email"]?.let { sanitizeHtml(it) }
                val name = formParameters["name"]?.let { sanitizeHtml(it) }
                val surname = formParameters["surname"]?.let { sanitizeHtml(it) }
                val roleCode = formParameters["roleCode"]?.toIntOrNull()
                val role = if (roleCode == null) null else Employee.Role.fromInt(roleCode)
                val isActive = formParameters["isActive"].toBoolean()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }
                if(!phone.isNullOrEmpty() && !isPhoneValid(phone)){
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@patch
                }
                if(!email.isNullOrEmpty() && !isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@patch
                }
                if(!name.isNullOrEmpty() && !isUsernameValid(name)){
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@patch
                }
                if(!surname.isNullOrEmpty() && !isUsernameValid(surname)){
                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                    return@patch
                }
                val ret = DaoMethods.editEmployee(id, name, surname, phone,email, role, isActive)
                if(ret.first){
                    call.respond(HttpStatusCode.OK,"The employee has been edited.")
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to edit employee. ${ret.second}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to edit employee. ${e.message}"
                )
            }
        }
        get("/getById"){
            try{
                val id = call.request.queryParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }

                val employee = DaoMethods.getEmployee(id)
                if(employee!=null){
                    call.respond(HttpStatusCode.OK,employee.toEmployeeInfo())
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get employee.")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get employee. ${e.message}"
                )
            }
        }
        get("/getByCredentials"){
            try{
                val login = call.request.queryParameters["login"]?.let { sanitizeHtml(it) }
                val password = call.request.queryParameters["password"]?.let { sanitizeHtml(it) }

                if(login.isNullOrEmpty() || password.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@get
                }
                val ret = DaoMethods.getCustomer(login.toString(), password.toString())
                if(ret.second != null){
                    call.respond(HttpStatusCode.OK, ret.second!!)
                }else{
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get employee. ${ret.first}")
                }
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get employee. ${e.message}"
                )
            }
        }
        get("/getPage"){
            try{
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val filterColumn = call.request.queryParameters["filterColumn"]?.let { sanitizeHtml(it) }
                val filterValue = call.request.queryParameters["filterValue"]?.let { sanitizeHtml(it) }
                val filterType = call.request.queryParameters["filterType"]?.let { sanitizeHtml(it) }
                val sortColumn = call.request.queryParameters["sortColumn"]?.let { sanitizeHtml(it) }
                val sortDir = call.request.queryParameters["sortDir"]?.let { sanitizeHtml(it) }
                val employees = DaoMethods.getEmployees(page,size, EmployeeField[filterColumn], filterValue,filterType, EmployeeField[sortColumn],sortDir)
                call.respond(HttpStatusCode.OK,employees)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get employees. ${e.message}"
                )
            }
        }
        get("/getAll"){
            try{
                val employees = DaoMethods.getAllEmployees()
                call.respond(HttpStatusCode.OK,employees)
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to get employees. ${e.message}"
                )
            }
        }

        patch("/restore"){
            try{
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@patch
                }

                val restored = DaoMethods.restoreEmployee(id)
                if(restored)
                    call.respond(HttpStatusCode.OK,"The employee has been restored.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to restore employee.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to restore employee. ${e.message}"
                )
            }
        }
        delete{
            try {
                val formParameters = call.receiveParameters()
                val id = formParameters["id"]?.toIntOrNull()

                if (id == null){
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@delete
                }

                val deleted = DaoMethods.deleteEmployee(id)
                if (deleted)
                    call.respond(HttpStatusCode.OK, "The employee has been deleted.")
                else
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete employee.")
            }catch(e:Error){
                call.respond(
                    HttpStatusCode.InternalServerError, "Failed to delete employee. ${e.message}"
                )
            }
        }
    }
}
