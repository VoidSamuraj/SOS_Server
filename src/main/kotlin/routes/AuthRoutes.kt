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
import security.getAccountId
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

/**
 * Creates JWTToken and save it in session
 *
 * @param employee
 * @return JWTToken?
 */
fun PipelineContext<Unit,ApplicationCall>.generateAndSetToken(employee: Employee): JWTToken?{
    val token = createToken(employee).first
    call.sessions.set(token)
    return token
}
/**
 * Creates JWTToken and save it in session
 *
 * @param customer
 * @return JWTToken?
 */
fun PipelineContext<Unit,ApplicationCall>.generateAndSetToken(customer: Customer): JWTToken?{
    val token = createToken(customer).first
    call.sessions.set(token)
    return token
}
/**
 * Creates JWTToken and save it in session
 *
 * @param guard
 * @return JWTToken?
 */
fun PipelineContext<Unit,ApplicationCall>.generateAndSetToken(guard: Guard): JWTToken?{
    val token = createToken(guard).first
    call.sessions.set(token)
    return token
}

fun Route.authRoutes(){

    route("/auth") {
        route("/employee") {
            post("/login") {
                val formParameters = call.receiveParameters()
                val login = formParameters.getOrFail("login")
                val password = formParameters.getOrFail("password")
                val employee = DaoMethods.getEmployee(login, password)
                if (employee.second != null) {
                    generateAndSetToken(employee.second!!)
                    call.respond(HttpStatusCode.OK, employee.second!!.toEmployeeInfo())
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }
            post("/refresh-token-expiration") {
                val token = call.sessions.get("userToken") as JWTToken?
                checkPermission(token = token,
                    onSuccess = {
                        val decodedToken = decodeToken(token?.token)

                        if (decodedToken == null) {
                            call.respond(HttpStatusCode.BadRequest, "Wrong token")
                            return@checkPermission
                        }
                        val employee = Employee(
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
                        getTokenExpirationDate(generateAndSetToken(employee))?.time?.let { it1 ->
                            call.attributes.put(
                                AttributeKey("exp"),
                                it1.toString()
                            )
                        }

                        call.respond(HttpStatusCode.OK, "Success")
                    },
                    onFailure = { }
                )
            }
            post("/logout") {
                call.sessions.clear("userToken")
                call.respond(HttpStatusCode.OK, "Success")
            }

            post("/register") {
                val formParameters = call.receiveParameters()
                val phone = formParameters["phone"]
                val email = formParameters["email"]
                val name = formParameters["name"]
                val surname = formParameters["surname"]
                val roleCode = formParameters["roleCode"]?.toIntOrNull()

                val login = generateRandomLogin()
                val password = generateRandomPassword()
                if (phone.isNullOrEmpty() || name.isNullOrEmpty() || surname.isNullOrEmpty() || roleCode == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
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

                if (ret.first && ret.third != null) {
                    Mailer.sendNewAccountEmail(
                        ret.third!!.name,
                        ret.third!!.surname,
                        ret.third!!.email,
                        login,
                        password
                    )

                    call.respond(HttpStatusCode.OK, "Employee added to database.")
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to add entry to the database. ${ret.second}"
                    )
                }
            }

            post("/remind-password") {
                val formParameters = call.receiveParameters()
                val email = formParameters.getOrFail("email")
                val employee = DaoMethods.getEmployee(email)
                if (employee != null) {
                    val host = call.request.origin.serverHost
                    val port = call.request.port()
                    val token = createToken(employee)
                    remindPasswordTokens.add(token)
                    Mailer.sendPasswordRestorationEmail(
                        employee.name,
                        employee.surname,
                        employee.email,
                        "http://$host:$port/reset-password?token=${token.first.token}"
                    )
                    call.respond(HttpStatusCode.OK, "Success")
                } else
                    call.respond(HttpStatusCode.NotFound, "Account with this email does not exist")

            }
            post("/reset-password") {
                val formParameters = call.receiveParameters()
                val newPassword = formParameters.getOrFail("password")
                val tokenString = formParameters.getOrFail("token")
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    val token = JWTToken(tokenString)
                    checkPermission(token, {

                        val id = getAccountId(token)
                        if (id != null && remindPasswordTokens.any { it.first == token }) {
                            val result = DaoMethods.changeEmployeePassword(id, newPassword)
                            if (result.first) {
                                remindPasswordTokens.removeIf { it.first == token }
                                call.sessions.clear("userToken")
                                call.respond(HttpStatusCode.OK, "Success")
                            } else {

                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    "Error during changing password: ${result.second}"
                                )
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Wrong token")
                        }

                    }, {
                        remindPasswordTokens.removeIf { it.first == token }
                        call.respond(HttpStatusCode.RequestTimeout, "Wrong Token or it has expired")
                    })

                } else {

                    call.respond(HttpStatusCode.BadRequest, "Wrong password or token")
                }
            }
        }

        route("/client") {
            post("/login") {
                val formParameters = call.receiveParameters()
                val login = formParameters.getOrFail("login")
                val password = formParameters.getOrFail("password")
                val customer = DaoMethods.getCustomer(login, password)
                if (customer.second != null) {
                    generateAndSetToken(customer.second!!)
                    call.respond(HttpStatusCode.OK, customer.second!!.toCustomerInfo())
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }

            post("/logout") {
                call.sessions.clear("userToken")
                call.respond(HttpStatusCode.OK, "Success")
            }

            post("/register") {
                val formParameters = call.receiveParameters()
                val login = formParameters["login"]
                val password = formParameters["password"]
                val phone = formParameters["phone"]
                val pesel = formParameters["pesel"]
                val email = formParameters["email"]
                val name = formParameters["name"]
                val surname = formParameters["surname"]
                if (login.isNullOrEmpty() || password.isNullOrEmpty() || name.isNullOrEmpty() || surname.isNullOrEmpty() || phone.isNullOrEmpty() || pesel.isNullOrEmpty() || email.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                val ret = DaoMethods.addCustomer(
                    login.toString(),
                    password.toString(),
                    name.toString(),
                    surname.toString(),
                    phone.toString(),
                    pesel.toString(),
                    email.toString()
                )

                if (ret.first && ret.third != null) {
                    Mailer.sendNewAccountEmail(
                        ret.third!!.name,
                        ret.third!!.surname,
                        ret.third!!.email,
                        login,
                        password
                    )

                    call.respond(HttpStatusCode.OK, "Client added to database.")
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to add entry to the database. ${ret.second}"
                    )
                }
            }

            post("/remind-password") {
                val formParameters = call.receiveParameters()
                val email = formParameters.getOrFail("email")
                val customer = DaoMethods.getCustomer(email)
                if (customer != null) {
                    val host = call.request.origin.serverHost
                    val port = call.request.port()
                    val token = createToken(customer)
                    remindPasswordTokens.add(token)
                    Mailer.sendPasswordRestorationEmail(
                        customer.name,
                        customer.surname,
                        customer.email,
                        "http://$host:$port/reset-password?token=${token.first.token}"
                    )
                    call.respond(HttpStatusCode.OK, "Success")
                } else
                    call.respond(HttpStatusCode.NotFound, "Account with this email does not exist")

            }
            post("/reset-password") {
                val formParameters = call.receiveParameters()
                val newPassword = formParameters.getOrFail("password")
                val tokenString = formParameters.getOrFail("token")
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    val token = JWTToken(tokenString)
                    checkPermission(token, {
                        val id = getAccountId(token)
                        if (id != null && remindPasswordTokens.any { it.first == token }) {
                            val result = DaoMethods.changeEmployeePassword(id, newPassword)
                            if (result.first) {
                                remindPasswordTokens.removeIf { it.first == token }
                                call.sessions.clear("userToken")
                                call.respond(HttpStatusCode.OK, "Success")
                            } else {
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    "Error during changing password: ${result.second}"
                                )
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Wrong token")
                        }
                    }, {
                        remindPasswordTokens.removeIf { it.first == token }
                        call.respond(HttpStatusCode.RequestTimeout, "Wrong Token or it has expired")
                    })
                } else {

                    call.respond(HttpStatusCode.BadRequest, "Wrong password or token")
                }
            }
        }

        route("/guard") {
            post("/login") {
                val formParameters = call.receiveParameters()
                val login = formParameters.getOrFail("login")
                val password = formParameters.getOrFail("password")
                val customer = DaoMethods.getGuard(login, password)
                if (customer.second != null) {
                    generateAndSetToken(customer.second!!)
                    call.respond(HttpStatusCode.OK, customer.second!!.toGuardInfo())
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }

            post("/logout") {
                call.sessions.clear("userToken")
                call.respond(HttpStatusCode.OK, "Success")
            }

            post("/register") {
                val formParameters = call.receiveParameters()
                val login = formParameters["login"]
                val password = formParameters["password"]
                val phone = formParameters["phone"]
                val pesel = formParameters["pesel"]
                val email = formParameters["email"]
                val name = formParameters["name"]
                val surname = formParameters["surname"]
                if (login.isNullOrEmpty() || password.isNullOrEmpty() || name.isNullOrEmpty() || surname.isNullOrEmpty() || phone.isNullOrEmpty() || pesel.isNullOrEmpty() || email.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                val ret = DaoMethods.addGuard(
                    login.toString(),
                    password.toString(),
                    name.toString(),
                    surname.toString(),
                    phone.toString(),
                    email.toString()
                )

                if (ret.first && ret.third != null) {
                    Mailer.sendNewAccountEmail(
                        ret.third!!.name,
                        ret.third!!.surname,
                        ret.third!!.email,
                        login,
                        password
                    )

                    call.respond(HttpStatusCode.OK, "Guard added to database.")
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to add entry to the database. ${ret.second}"
                    )
                }
            }

            post("/remind-password") {
                val formParameters = call.receiveParameters()
                val email = formParameters.getOrFail("email")
                val guard = DaoMethods.getGuard(email)
                if (guard != null) {
                    val host = call.request.origin.serverHost
                    val port = call.request.port()
                    val token = createToken(guard)
                    remindPasswordTokens.add(token)
                    Mailer.sendPasswordRestorationEmail(
                        guard.name,
                        guard.surname,
                        guard.email,
                        "http://$host:$port/reset-password?token=${token.first.token}"
                    )
                    call.respond(HttpStatusCode.OK, "Success")
                } else
                    call.respond(HttpStatusCode.NotFound, "Account with this email does not exist")

            }
            post("/reset-password") {
                val formParameters = call.receiveParameters()
                val newPassword = formParameters.getOrFail("password")
                val tokenString = formParameters.getOrFail("token")
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    val token = JWTToken(tokenString)
                    checkPermission(token, {
                        val id = getAccountId(token)
                        if (id != null && remindPasswordTokens.any { it.first == token }) {
                            val result = DaoMethods.changeGuardPassword(id, newPassword)
                            if (result.first) {
                                remindPasswordTokens.removeIf { it.first == token }
                                call.sessions.clear("userToken")
                                call.respond(HttpStatusCode.OK, "Success")
                            } else {
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    "Error during changing password: ${result.second}"
                                )
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Wrong token")
                        }
                    }, {
                        remindPasswordTokens.removeIf { it.first == token }
                        call.respond(HttpStatusCode.RequestTimeout, "Wrong Token or it has expired")
                    })
                } else {

                    call.respond(HttpStatusCode.BadRequest, "Wrong password or token")
                }
            }
        }
    }
}

