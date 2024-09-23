package routes

import Employee
import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.request.port
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.util.getOrFail
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import jwtExpirationSeconds
import kotlinx.serialization.Serializable
import plugins.Mailer
import plugins.checkPermission
import plugins.createToken
import plugins.decodeToken
import plugins.generateRandomLogin
import plugins.generateRandomPassword
import plugins.getEmployeeId
import plugins.getTokenExpirationDate
import security.JWTToken

@Serializable
data class ResponseObject(
    val message: String,
    val exp: String,
    val user: EmployeeInfo
)

suspend fun PipelineContext<Unit, ApplicationCall>.checkUserPermission(onSuccess:suspend ()->Unit){
    val token=call.sessions.get("userToken")as JWTToken?
    checkPermission(token = token,
        onSuccess = {
            onSuccess()
        },
        onFailure = {
            call.respondRedirect("/")
        })
}

fun PipelineContext<Unit,ApplicationCall>.generateToken(employee: Employee): JWTToken?{
    val token = createToken(employee)
    call.sessions.set(token)
    return token
}

suspend fun PipelineContext<Unit,ApplicationCall>.onAuthenticate(employee: Employee){


    val token = generateToken(employee) ?: return
    getTokenExpirationDate(token)?.let { expirationDate ->
        call.attributes.put(AttributeKey("exp"), expirationDate.time.toString())
    }
    val responseObject = ResponseObject(
        message = "Success",
        exp = (System.currentTimeMillis() + jwtExpirationSeconds * 1000).toString(),
        user = employee.toEmployeeInfo()
    )
    call.respond(HttpStatusCode.OK, responseObject)
}
suspend fun PipelineContext<Unit,ApplicationCall>.onAuthError(){
    call.respondRedirect("/")
}

fun Route.authRoutes(){


    route("/employee"){
        post("/login") {
            val formParameters = call.receiveParameters()
            val login = formParameters.getOrFail("login")
            val password = formParameters.getOrFail("password")
            val employee= DaoMethods.getEmployee(login, password)
            if(employee.second!=null) {
                onAuthenticate(employee.second!!)
            }else {
               // errorMessage =employee.first
                onAuthError()
            }
        }
        post("/refresh-token-expiration"){
            val token=call.sessions.get("userToken")as JWTToken?
            checkPermission(token = token,
                onSuccess = {
                    val decodedToken= decodeToken(token?.token)
                    val employee=Employee(
                        decodedToken.claims["id"]!!.asInt(),
                        decodedToken.claims["login"]!!.asString(),
                        "",
                        decodedToken.claims["name"]!!.asString(),
                        decodedToken.claims["surname"]!!.asString(),
                        decodedToken.claims["phone"]!!.asString(),
                        decodedToken.claims["email"]!!.asString(),
                        decodedToken.claims["roleCode"]!!.asInt().toShort(),
                        false
                    )
                    getTokenExpirationDate(generateToken(employee))?.time?.let { it1 -> call.attributes.put(AttributeKey("exp"), it1.toString()) }

                    call.respond(HttpStatusCode.OK, "Success")
                },
                onFailure = { call.respondRedirect("/user/login")}
            )
        }
        post("/logout") {
            call.sessions.clear("userToken")
            call.respond(HttpStatusCode.OK, "Success")
        }

        //TODO add verify if employee has permission to edit
        post("/register"){
            val formParameters = call.receiveParameters()
            val phone = formParameters["phone"]
            val email = formParameters["email"]
            val name = formParameters["name"]
            val surname = formParameters["surname"]
            val roleCode = formParameters["roleCode"]?.toIntOrNull()

            val login = generateRandomLogin()
            val password = generateRandomPassword()
            if(phone.isNullOrEmpty() || name.isNullOrEmpty() || surname.isNullOrEmpty() ||  roleCode == null)
                call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
            val ret = DaoMethods.addEmployee(login.toString(), password.toString(), name.toString(), surname.toString(), phone.toString(), email.toString(), Employee.Role.fromInt(roleCode!!))

            if(ret.first && ret.third != null){
                Mailer.sendNewAccountEmail(
                    ret.third!!.name,
                    ret.third!!.surname,
                    ret.third!!.email,
                    login,
                    password
                )

                call.respond(HttpStatusCode.OK,"Employee added to database.")
            }else{
                call.respond(HttpStatusCode.InternalServerError, "Failed to add entry to the database. ${ret.second}")
            }
        }

        post("/remind-password"){
            val formParameters = call.receiveParameters()
            val email = formParameters.getOrFail("email")
            val employee= DaoMethods.getEmployee(email)
            if(employee!=null){
                val host = call.request.origin.serverHost
                val port = call.request.port()
                val token = generateToken(employee)
                if(token!=null) {
                    Mailer.sendPasswordRestorationEmail(
                        employee.name,
                        employee.surname,
                        employee.email,
                        "http://$host:$port/reset-password?token=${token.token}"
                    )
                    call.respond(HttpStatusCode.OK, "Success")
                }else
                 call.respond(HttpStatusCode.InternalServerError, "Error generating token")
            }else
                call.respond(HttpStatusCode.NotFound, "Account with this email does not exist")

        }
        post("/reset-password"){
            val formParameters = call.receiveParameters()
            val newPassword = formParameters.getOrFail("password")
            val tokenString = formParameters.getOrFail("token")
                if(newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    val token=JWTToken(tokenString)
                    checkPermission(token,{
                        val sessionToken=call.sessions.get("userToken")as JWTToken?
                        if (sessionToken==null ||  sessionToken.token!= tokenString) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid or expired token")
                        }
                        val id = getEmployeeId(token)
                        if(id!=null) {
                            val result = DaoMethods.changeEmployeePassword(id, newPassword)
                            if(result.first) {
                                call.sessions.clear("userToken")
                                call.respond(HttpStatusCode.OK, "Success")
                            }else
                                call.respond(HttpStatusCode.InternalServerError, "Error during changing password: ${result.second}")

                        }else
                            call.respond(HttpStatusCode.BadRequest, "Wrong token")
                    },{
                        call.respond(HttpStatusCode.RequestTimeout, "Token has expired")
                    })


                }else
                    call.respond(HttpStatusCode.BadRequest, "Wrong password or token")
        }
    }
}

