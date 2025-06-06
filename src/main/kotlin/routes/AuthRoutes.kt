package routes

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
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.util.getOrFail
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import models.dto.Credentials
import models.dto.TokenResponse
import models.entity.Employee
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

private val remindPasswordTokens = mutableListOf<Pair<JWTToken, Long>>()

/**
 * Remove expired tokens from list of tokens generated for password recovery
 */
fun scheduleTokenCleanup() {
    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            delay(3600_000L)
            remindPasswordTokens.removeIf { it.second < System.currentTimeMillis() }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkUserPermission(
    onSuccess: suspend (role: Employee.Role? ) -> Unit,
    onFailure: (suspend () -> Unit)? = null,
    roles: List<Employee.Role>?=null
) {
    val token = call.sessions.get("userToken") as JWTToken?
    checkPermission(
        token = token,
        roles = roles,
        onSuccess = { role->
            onSuccess(role)
        },
        onFailure = {
            if (onFailure != null)
                onFailure.invoke()
            else
                call.respondRedirect("/login")
        })
}

suspend fun checkUserPermission(
    refreshToken: JWTToken,
    onSuccess: suspend (role: Employee.Role? ) -> Unit,
    onFailure: (suspend () -> Unit)? = null
) {

    checkPermission(
        token = refreshToken,
        onSuccess = {role->
            onSuccess(role)
        },
        onFailure = {
            onFailure?.invoke()
        })
}

/**
 * Creates JWTToken and save it in session
 *
 * @param employee
 * @return JWTToken?
 */
fun PipelineContext<Unit, ApplicationCall>.generateAndSetToken(employee: Employee): JWTToken? {
    val token = createToken(employee).first
    call.sessions.set(token)
    return token
}

fun Route.authRoutes() {

    route("/auth") {
        route("/employee") {
            post("/login") {
                val formParameters = call.receiveParameters()
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val employee = DaoMethods.getEmployee(login, password)
                if (employee.second != null) {
                    val exp = getTokenExpirationDate(generateAndSetToken(employee.second!!))?.time
                    call.respond(HttpStatusCode.OK, Pair(employee.second!!.toEmployeeInfo(),exp))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }
            post("/refresh-token-expiration") {
                val token = call.sessions.get("userToken") as JWTToken?
                checkPermission(
                    token = token,
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
                        val exp = getTokenExpirationDate(generateAndSetToken(employee))?.time
                        if(exp!=null)
                            call.respond(HttpStatusCode.OK, exp)
                        else
                            call.respond(HttpStatusCode.Unauthorized)

                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
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
                if (!isLoginValid(login)) {
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@post
                }
                if (!isPasswordValid(password)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                    )
                    return@post
                }
                if (!isPhoneValid(phone)) {
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@post
                }
                if (!isEmailValid(email)) {
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
                if (!isUsernameValid(name)) {
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@post
                }
                if (!isUsernameValid(surname)) {
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
                if (!isEmailValid(email)) {
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
                    if (!isPasswordValid(newPassword)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                        )
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
        //todo
        //remove userToken in
        route("/client") {
            get("/isLoginUsed") {
                val login = call.request.queryParameters["login"]?.let { sanitizeHtml(it) }
                if (login != null) {
                    val isLoginUsed = DaoMethods.isCustomerLoginUsed(login)
                    call.respond(HttpStatusCode.OK, isLoginUsed)
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No login provided")
                }
            }

            post("/login") {
                val loginRequest = call.receive<Credentials>()
                val login = sanitizeHtml(loginRequest.login)
                val password = sanitizeHtml(loginRequest.password)

                val customer = DaoMethods.getCustomer(login, password)
                if (customer.second != null) {
                    val token = createToken(customer.second!!).first
                    val longToken = createToken(customer.second!!, longTime = true).first
                    val client = customer.second!!.toCustomerInfo()
                    client.token = token.token
                    call.respond(HttpStatusCode.OK, Triple(customer.second!!.login, client, longToken))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }

            post("/logout") {
                //todo add status change
                call.respond(HttpStatusCode.OK, "Success")
            }

            post("/refresh_token") {
                val refreshToken = JWTToken(call.receive<String>())
                checkUserPermission(
                    refreshToken = refreshToken,
                    onSuccess = {
                        val customer =
                            getAccountId(refreshToken)?.let { customerId -> DaoMethods.getCustomer(customerId) }
                        if (customer != null) {
                            val token = createToken(customer).first
                            call.respond(HttpStatusCode.OK, TokenResponse(token.token, null))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Failed to refresh token")
                        }
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "Failed to refresh token")
                    })
            }

            post("/refresh_refresh_token") {
                val refreshToken = JWTToken(call.receive<String>())
                checkUserPermission(
                    refreshToken = refreshToken,
                    onSuccess = {
                        val customer =
                            getAccountId(refreshToken)?.let { customerId -> DaoMethods.getCustomer(customerId) }
                        if (customer != null) {
                            val token = createToken(customer).first
                            val refreshToken = createToken(customer, longTime = true).first
                            call.respond(HttpStatusCode.OK, TokenResponse(token.token, refreshToken.token))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Failed to refresh token")
                        }
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "Failed to refresh token")
                    })
            }
            post("/checkToken") {
                val token = JWTToken(call.receive<String>())
                checkPermission(
                    token,
                    onSuccess = {
                        val customer = getAccountId(token)?.let { id ->
                            DaoMethods.getCustomer(id)
                        }
                        val token = customer?.let { customer -> createToken(customer).first }
                        if (token != null) {
                            val client = customer.toCustomerInfo()
                            client.token = token.token
                            call.respond(HttpStatusCode.OK, Pair(customer.login, client))
                        } else
                            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
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
                if (!isLoginValid(login)) {
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@post
                }
                if (!isPasswordValid(password)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                    )
                    return@post
                }
                if (!isPhoneValid(phone)) {
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@post
                }

                if (!isEmailValid(email)) {
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }
                if (!isPeselValid(pesel)) {
                    call.respond(HttpStatusCode.BadRequest, "Pesel is in wrong format")
                    return@post
                }
                if (!isUsernameValid(name)) {
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@post
                }
                if (!isUsernameValid(surname)) {
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
                    val token = createToken(ret.third!!).first
                    val longToken = createToken(ret.third!!, longTime = true).first
                    val client = ret.third!!.toCustomerInfo()
                    client.token = token.token
                    call.respond(HttpStatusCode.OK, Triple(ret.third!!.login, client, longToken))
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
                if (!isEmailValid(email)) {
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

            patch("/edit") {
                val authHeader = call.request.headers["Authorization"]
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    val token = authHeader.removePrefix("Bearer ")
                    checkPermission(
                        JWTToken(token),
                        onSuccess = {
                            try {
                                val formParameters = call.receiveParameters()
                                val id = formParameters["id"]?.toIntOrNull()
                                val login =
                                    formParameters["login"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val password =
                                    formParameters["password"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val newPassword =
                                    formParameters["newPassword"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val name = formParameters["name"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val surname =
                                    formParameters["surname"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val phone =
                                    formParameters["phone"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val pesel =
                                    formParameters["pesel"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val email =
                                    formParameters["email"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val protectionExpirationDate =
                                    formParameters["protection_expiration_date"]?.let { sanitizeHtml(it) }
                                val protectionExpirationDateTime =
                                    protectionExpirationDate?.let { LocalDateTime.parse(it.toString()) }

                                if (id == null || password.isNullOrEmpty()) {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        "Invalid input: required fields are missing or null."
                                    )
                                    return@checkPermission
                                }
                                if (!login.isNullOrEmpty() && !isLoginValid(login)) {
                                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                                    return@checkPermission
                                }
                                if (!phone.isNullOrEmpty() && !isPhoneValid(phone)) {
                                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                                    return@checkPermission
                                }
                                if (!newPassword.isNullOrEmpty() && !isPasswordValid(newPassword)) {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                                    )
                                    return@checkPermission
                                }
                                if (!email.isNullOrEmpty() && !isEmailValid(email)) {
                                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                                    return@checkPermission
                                }
                                if (!pesel.isNullOrEmpty() && !isPeselValid(pesel)) {
                                    call.respond(HttpStatusCode.BadRequest, "Pesel is in wrong format")
                                    return@checkPermission
                                }
                                if (!name.isNullOrEmpty() && !isUsernameValid(name)) {
                                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                                    return@checkPermission
                                }
                                if (!surname.isNullOrEmpty() && !isUsernameValid(surname)) {
                                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                                    return@checkPermission
                                }
                                val ret = DaoMethods.editCustomer(
                                    id,
                                    login,
                                    password.toString(),
                                    newPassword,
                                    name,
                                    surname,
                                    phone,
                                    pesel,
                                    email,
                                    protectionExpirationDateTime
                                )
                                if (ret.second != null) {
                                    val token = createToken(ret.second!!).first
                                    val client = ret.second!!.toCustomerInfo()
                                    client.token = token.token
                                    call.respond(HttpStatusCode.OK, Pair(ret.second!!.login, client))
                                } else {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        "Failed to edit client. ${ret.first}"
                                    )
                                }
                            } catch (e: Error) {
                                call.respond(
                                    HttpStatusCode.InternalServerError, "Failed to edit client. ${e.message}"
                                )
                            }
                        },
                        onFailure = {
                            call.respond(HttpStatusCode.Unauthorized, "Failed to edit client.")
                        })
                }

            }
            //todo change for customer
            post("/reset-password") {
                val formParameters = call.receiveParameters()
                val newPassword = sanitizeHtml(formParameters.getOrFail("password"))
                val tokenString = sanitizeHtml(formParameters.getOrFail("token"))
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    if (!isPasswordValid(newPassword)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                        )
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

            /**
             * Route to check if there is an account with specified Login in DB
             */
            get("/isLoginUsed") {
                val login = call.request.queryParameters["login"]?.let { sanitizeHtml(it) }
                if (login != null) {
                    val isLoginUsed = DaoMethods.isGuardLoginUsed(login)
                    call.respond(HttpStatusCode.OK, isLoginUsed)
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No login provided")
                }
            }

            post("/login") {
                val loginRequest = call.receive<Credentials>()
                val login = sanitizeHtml(loginRequest.login)
                val password = sanitizeHtml(loginRequest.password)

                val guard = DaoMethods.getGuard(login, password)
                if (guard.second != null) {
                    val token = createToken(guard.second!!).first
                    val longToken = createToken(guard.second!!, longTime = true).first
                    val editedGuard = guard.second!!.toGuardInfo()
                    editedGuard.token = token.token
                    call.respond(HttpStatusCode.OK, Triple(guard.second!!.login, editedGuard, longToken))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong credentials")
                }
            }

            post("/logout") {
                //todo add status change
                call.respond(HttpStatusCode.OK, "Success")
            }

            post("/checkToken") {
                val token = JWTToken(call.receive<String>())
                checkPermission(
                    token,
                    onSuccess = {
                        val guard = getAccountId(token)?.let { id ->
                            DaoMethods.getGuard(id)
                        }
                        val token = guard?.let { guard -> createToken(guard).first }
                        if (token != null) {
                            val editedGuard = guard.toGuardInfo()
                            editedGuard.token = token.token
                            call.respond(HttpStatusCode.OK, Pair(guard.login, editedGuard))
                        } else
                            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    }
                )
            }
            post("/refresh_token") {
                val refreshToken = JWTToken(call.receive<String>())
                checkUserPermission(
                    refreshToken = refreshToken,
                    onSuccess = {
                        val guard = getAccountId(refreshToken)?.let { guardId -> DaoMethods.getGuard(guardId) }
                        if (guard != null) {
                            val token = createToken(guard).first
                            call.respond(HttpStatusCode.OK, TokenResponse(token.token, null))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Failed to refresh token")
                        }
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "Failed to refresh token")
                    })
            }

            post("/refresh_refresh_token") {
                val refreshToken = JWTToken(call.receive<String>())
                checkUserPermission(
                    refreshToken = refreshToken,
                    onSuccess = {
                        val guard = getAccountId(refreshToken)?.let { guardId -> DaoMethods.getGuard(guardId) }
                        if (guard != null) {
                            val token = createToken(guard).first
                            val refreshToken = createToken(guard, longTime = true).first
                            call.respond(HttpStatusCode.OK, TokenResponse(token.token, refreshToken.token))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Failed to refresh token")
                        }
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.Unauthorized, "Failed to refresh token")
                    })
            }
            post("/register") {
                val formParameters = call.receiveParameters()
                val login = sanitizeHtml(formParameters.getOrFail("login"))
                val password = sanitizeHtml(formParameters.getOrFail("password"))
                val phone = sanitizeHtml(formParameters.getOrFail("phone"))
                val email = sanitizeHtml(formParameters.getOrFail("email"))
                val name = sanitizeHtml(formParameters.getOrFail("name"))
                val surname = sanitizeHtml(formParameters.getOrFail("surname"))
                if (login.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input: required fields are missing or null.")
                    return@post
                }
                if (!isLoginValid(login)) {
                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                    return@post
                }
                if (!isPasswordValid(password)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                    )
                    return@post
                }
                if (!isPhoneValid(phone)) {
                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                    return@post
                }

                if (!isEmailValid(email)) {
                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                    return@post
                }

                if (!isUsernameValid(name)) {
                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                    return@post
                }
                if (!isUsernameValid(surname)) {
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
                    val token = createToken(ret.third!!).first
                    val longToken = createToken(ret.third!!, longTime = true).first
                    val editedGuard = ret.third!!.toGuardInfo()
                    editedGuard.token = token.token
                    call.respond(HttpStatusCode.OK, Triple(ret.third!!.login, editedGuard, longToken))
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
                if (!isEmailValid(email)) {
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

            patch("/edit") {
                val authHeader = call.request.headers["Authorization"]
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    val token = authHeader.removePrefix("Bearer ")
                    checkPermission(
                        JWTToken(token),
                        onSuccess = {
                            try {
                                val formParameters = call.receiveParameters()
                                val id = formParameters["id"]?.toIntOrNull()
                                val login =
                                    formParameters["login"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val password =
                                    formParameters["password"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val newPassword =
                                    formParameters["newPassword"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val name = formParameters["name"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val surname =
                                    formParameters["surname"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val phone =
                                    formParameters["phone"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }
                                val email =
                                    formParameters["email"]?.let { sanitizeHtml(it) }?.takeIf { it.isNotEmpty() }

                                if (id == null || password.isNullOrEmpty()) {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        "Invalid input: required fields are missing or null."
                                    )
                                    return@checkPermission
                                }
                                if (!login.isNullOrEmpty() && !isLoginValid(login)) {
                                    call.respond(HttpStatusCode.BadRequest, "Login should have length between 3 and 20")
                                    return@checkPermission
                                }
                                if (!phone.isNullOrEmpty() && !isPhoneValid(phone)) {
                                    call.respond(HttpStatusCode.BadRequest, "Phone is in wrong format")
                                    return@checkPermission
                                }
                                if (!newPassword.isNullOrEmpty() && !isPasswordValid(newPassword)) {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                                    )
                                    return@checkPermission
                                }
                                if (!email.isNullOrEmpty() && !isEmailValid(email)) {
                                    call.respond(HttpStatusCode.BadRequest, "Email is in wrong format")
                                    return@checkPermission
                                }

                                if (!name.isNullOrEmpty() && !isUsernameValid(name)) {
                                    call.respond(HttpStatusCode.BadRequest, "Name is in wrong format")
                                    return@checkPermission
                                }
                                if (!surname.isNullOrEmpty() && !isUsernameValid(surname)) {
                                    call.respond(HttpStatusCode.BadRequest, "Surname is in wrong format")
                                    return@checkPermission
                                }
                                val ret =
                                    DaoMethods.editGuard(
                                        id,
                                        login,
                                        password.toString(),
                                        newPassword,
                                        name,
                                        surname,
                                        phone,
                                        email
                                    )
                                if (ret.second != null) {
                                    val token = createToken(ret.second!!).first
                                    val guard = ret.second!!.toGuardInfo()
                                    guard.token = token.token
                                    call.respond(HttpStatusCode.OK, Pair(ret.second!!.login, guard))
                                } else {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        "Failed to edit client. ${ret.first}"
                                    )
                                }
                            } catch (e: Error) {
                                call.respond(
                                    HttpStatusCode.InternalServerError, "Failed to edit client. ${e.message}"
                                )
                            }
                        },
                        onFailure = {
                            call.respond(HttpStatusCode.Unauthorized, "Failed to edit client.")

                        })
                }

            }
            //todo change for guard
            post("/reset-password") {
                val formParameters = call.receiveParameters()
                val newPassword = sanitizeHtml(formParameters.getOrFail("password"))
                val tokenString = sanitizeHtml(formParameters.getOrFail("token"))
                if (newPassword.isNotEmpty() && tokenString.isNotEmpty()) {
                    if (!isPasswordValid(newPassword)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Password should contain one one upper and one lower case letter, one number, one special character and have min length 8"
                        )
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

