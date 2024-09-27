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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import plugins.Mailer
import plugins.generateRandomLogin
import plugins.generateRandomPassword
import security.JWTToken
import security.checkPermission
import security.createToken
import security.decodeToken
import security.getEmployeeId
import security.getTokenExpirationDate

private val remindPasswordTokens=mutableListOf<Pair<JWTToken, Long>>()

/**
 * Remove expired tokens from list of tokens generated for password recovery
 */
fun scheduleTokenCleanup() {
    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            delay(3600_000L)
            remindPasswordTokens.removeIf { it.second < System.currentTimeMillis() }
            println("Token cleanup completed")
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkUserPermission(onSuccess:suspend ()->Unit, onFailure: (suspend () -> Unit)? = null ){
    val token=call.sessions.get("userToken")as JWTToken?
    checkPermission(token = token,
        onSuccess = {
            onSuccess()
        },
        onFailure = {
            if(onFailure != null)
                onFailure.invoke()
            else
                call.respondRedirect("/login")
        })
}

fun PipelineContext<Unit,ApplicationCall>.generateAndSetToken(employee: Employee): JWTToken?{
    val token = createToken(employee).first
    call.sessions.set(token)
    return token
}

suspend fun PipelineContext<Unit,ApplicationCall>.onAuthenticate(employee: Employee){
    generateAndSetToken(employee)
    call.respond(HttpStatusCode.OK, employee.toEmployeeInfo())
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
                call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
            }
        }
        post("/refresh-token-expiration"){
            val token=call.sessions.get("userToken")as JWTToken?
            checkPermission(token = token,
                onSuccess = {
                    val decodedToken= decodeToken(token?.token)

                    if(decodedToken == null){
                        call.respond(HttpStatusCode.BadRequest, "Wrong token")
                        return@checkPermission
                    }
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
                    getTokenExpirationDate(generateAndSetToken(employee))?.time?.let { it1 -> call.attributes.put(AttributeKey("exp"), it1.toString()) }

                    call.respond(HttpStatusCode.OK, "Success")
                },
                onFailure = { }
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
                val token =  createToken(employee)
                remindPasswordTokens.add(token)
                Mailer.sendPasswordRestorationEmail(
                    employee.name,
                    employee.surname,
                    employee.email,
                    "http://$host:$port/reset-password?token=${token.first.token}"
                )
                call.respond(HttpStatusCode.OK, "Success")
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

                    val id = getEmployeeId(token)
                    if(id!=null &&  remindPasswordTokens.any { it.first == token }) {
                        val result = DaoMethods.changeEmployeePassword(id, newPassword)
                        if(result.first) {
                            remindPasswordTokens.removeIf { it.first == token }
                            call.sessions.clear("userToken")
                            call.respond(HttpStatusCode.OK, "Success")
                        }else {

                            call.respond(
                                HttpStatusCode.InternalServerError,
                                "Error during changing password: ${result.second}"
                            )
                        }
                    }else {

                        call.respond(HttpStatusCode.BadRequest, "Wrong token")
                    }

                },{
                    remindPasswordTokens.removeIf { it.first == token }
                    call.respond(HttpStatusCode.RequestTimeout, "Wrong Token or it has expired")
                })


            }else {

                call.respond(HttpStatusCode.BadRequest, "Wrong password or token")
            }
        }
    }
}

