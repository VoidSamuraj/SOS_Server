package routes

import Employee
import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.request.port
import io.ktor.server.request.receive
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
import models.Credentials
import plugins.Mailer
import plugins.generateRandomLogin
import plugins.generateRandomPassword
import plugins.isPeselValid
import plugins.sanitizeHtml
import plugins.isEmailValid
import plugins.isLoginValid
import plugins.isPasswordValid
import plugins.isPhoneValid
import plugins.isUsernameValid
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
    val token=call.sessions.get("userToken") as JWTToken?
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
                val login =  sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
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
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))
                val roleCode = formParameters["roleCode"]?.toIntOrNull()

                val login = generateRandomLogin()
                val password = generateRandomPassword()
                if (phone.isEmpty() || name.isEmpty() || surname.isEmpty() || roleCode == null) {
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
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                if(!isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
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
                val newPassword = sanitizeHtml(formParameters.getOrFail("password"))
                val tokenString = sanitizeHtml(formParameters.getOrFail("token"))
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    if(!isPasswordValid(newPassword)){
                        call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                        return@post
                    }
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
                val loginRequest = call.receive<Credentials>()
                val login = sanitizeHtml(loginRequest.login)
                val password = sanitizeHtml(loginRequest.password)

                val customer = DaoMethods.getCustomer(login, password)
                if (customer.second != null) {
                    val token = generateAndSetToken(customer.second!!)
                    val client = customer.second!!.toCustomerInfo()
                    token?.token?.let { client.token = it }
                    call.respond(HttpStatusCode.OK, client)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }

            post("/logout") {
                call.sessions.clear("userToken")
                call.respond(HttpStatusCode.OK, "Success")
            }
            post("/checkToken") {
                val token = call.receive<String>()
                checkPermission(JWTToken(token),
                    onSuccess = {
                    call.respond(HttpStatusCode.OK, "Success")
                },
                    onFailure = {
                        call.respond(HttpStatusCode.Forbidden, "Forbidden")
                    }
                )
            }

            post("/register") {
                val formParameters = call.receiveParameters()
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val pesel = sanitizeHtml(formParameters.getOrFail("pesel"))
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))
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
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                if(!isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
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
                val newPassword = sanitizeHtml(formParameters.getOrFail("password"))
                val tokenString = sanitizeHtml(formParameters.getOrFail("token"))
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    if(!isPasswordValid(newPassword)){
                        call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                        return@post
                    }
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
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
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
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val pesel = sanitizeHtml(formParameters.getOrFail("pesel"))
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))
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
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                if(!isEmailValid(email)){
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
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
                val newPassword = sanitizeHtml(formParameters.getOrFail("password"))
                val tokenString = sanitizeHtml(formParameters.getOrFail("token"))
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    if(!isPasswordValid(newPassword)){
                        call.respond(HttpStatusCode.BadRequest, "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8")
                        return@post
                    }
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

