package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import dao.DaoMethods
import jwtExpirationMilliSeconds
import kotlinx.serialization.Serializable
import longTokenExpirationTime
import models.dto.CustomerInfo
import models.dto.GuardInfo
import models.entity.Customer
import models.entity.Employee
import models.entity.Guard
import security.Keys.ISS
import java.util.Date

@Serializable
data class JWTToken(val token: String)


/**
 * Checks the validity of the provided JWT token and determines
 * if the associated account (employee, customer, or guard) has valid permissions.
 *
 * @param token The JWT token to validate.
 * @param onSuccess A suspend function to execute if the token is valid and the account exists.
 * @param onFailure A suspend function to execute if the token is invalid, expired, or not authorized.
 */
suspend fun checkPermission(
    token: JWTToken?,
    onSuccess: suspend () -> Unit,
    onFailure: suspend () -> Unit
) {

    if (token == null) {
        onFailure()
        return
    }

    val decodedToken = decodeToken(token.token)

    // Token verification
    if (decodedToken == null) {
        onFailure()
        return
    }
    val id = getAccountId(decodedToken)
    val audience = decodedToken.audience?.firstOrNull()
    val isDeleted = decodedToken.getClaim("isDeleted").asBoolean()

    if (id != null && !isDeleted)
        when (audience) {
            "employee" -> {
                val expireTime = getTokenExpirationDate(decodedToken)
                if (expireTime == null || expireTime.before(Date(System.currentTimeMillis()))) {
                    onFailure()
                    return
                }

                if (DaoMethods.getEmployee(id) != null) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }

            "customer" -> {
                if (DaoMethods.getCustomer(id) != null) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }

            "guard" -> {
                if (DaoMethods.getGuard(id) != null) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }

            else -> {
                // Unrecognized audience type
                onFailure()
            }
        }
    else
        onFailure()
}

/**
 * Creates a JWT token for a given customer with relevant claims.
 *
 * The generated token includes the following claims:
 * - `id`: The ID of the customer.
 * - `login`: The login of the customer.
 * - `name`: The name of the customer.
 * - `surname`: The surname of the customer.
 * - `phone`: The phone number of the customer.
 * - `email`: The email address of the customer.
 *
 * @param customer The employee for whom the token is created.
 * @param longTime The boolean deciding if token should have longer expiration time [longTokenExpirationTime] or [jwtExpirationMilliSeconds].
 * @return A Pair containing JWTToken and expiration date.
 */
fun createToken(customer: Customer, longTime: Boolean=false): Pair<JWTToken, Long> {
    val tokenExp = Date(System.currentTimeMillis() + if(longTime) longTokenExpirationTime else jwtExpirationMilliSeconds)
    val token = JWT.create()
        .withIssuer(ISS)
        .withAudience("customer")
        .withClaim("id", customer.id)
        .withClaim("login", customer.login)
        .withClaim("name", customer.name)
        .withClaim("surname", customer.surname)
        .withClaim("phone", customer.phone)
        .withClaim("email", customer.email)
        .withClaim("isDeleted",customer.account_deleted)
        .withExpiresAt(tokenExp)
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return Pair(JWTToken(token), Long.MAX_VALUE)
}

/**
 * Creates a JWT token for a given guard with relevant claims.
 *
 * The generated token includes the following claims:
 * - `id`: The ID of the guard.
 * - `login`: The login of the guard.
 * - `name`: The name of the guard.
 * - `surname`: The surname of the guard.
 * - `phone`: The phone number of the guard.
 * - `email`: The email address of the guard.
 *
 * @param guard The employee for whom the token is created.
 * @param longTime The boolean deciding if token should have longer expiration time [longTokenExpirationTime] or [jwtExpirationMilliSeconds].
 * @return A Pair containing JWTToken and expiration date.
 */
fun createToken(guard: Guard, longTime: Boolean=false): Pair<JWTToken, Long> {
    val tokenExp = Date(System.currentTimeMillis() + if(longTime) longTokenExpirationTime else jwtExpirationMilliSeconds)
    val token = JWT.create()
        .withIssuer(ISS)
        .withAudience("guard")
        .withClaim("id", guard.id)
        .withClaim("login", guard.login)
        .withClaim("name", guard.name)
        .withClaim("surname", guard.surname)
        .withClaim("phone", guard.phone)
        .withClaim("email", guard.email)
        .withClaim("isDeleted",guard.account_deleted)
        .withExpiresAt(tokenExp)
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return Pair(JWTToken(token), Long.MAX_VALUE)
}

/**
 * Creates a JWT token for a given employee with relevant claims.
 *
 * The generated token includes the following claims:
 * - `id`: The ID of the employee.
 * - `login`: The login of the employee.
 * - `name`: The name of the employee.
 * - `surname`: The surname of the employee.
 * - `phone`: The phone number of the employee.
 * - `email`: The email address of the employee.
 * - `roleCode`: The role code of the employee.
 * - `expiresAt`: The expiration date of the token.
 *
 * @param employee The employee for whom the token is created.
 * @return A Pair containing JWTToken and expiration date.
 */
fun createToken(employee: Employee): Pair<JWTToken, Long> {
    val tokenExp = Date(System.currentTimeMillis() + jwtExpirationMilliSeconds)
    val token = JWT.create()
        .withIssuer(ISS)
        .withAudience("employee")
        .withClaim("id", employee.id)
        .withClaim("login", employee.login)
        .withClaim("name", employee.name)
        .withClaim("surname", employee.surname)
        .withClaim("phone", employee.phone)
        .withClaim("email", employee.email)
        .withClaim("roleCode", employee.roleCode.toInt())
        .withClaim("isDeleted",employee.account_deleted)
        .withExpiresAt(tokenExp)
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return Pair(JWTToken(token), tokenExp.time)
}

/**
 * Decodes a JWT token and verifies its signature.
 *
 * @param jwtToken The JWT token to decode.
 * @return A DecodedJWT object containing the claims of the token, null if is not valid.
 */
fun decodeToken(jwtToken: String?): DecodedJWT? {
    return try {
        JWT.require(Algorithm.HMAC256(Keys.JWTSecret))
            .build()
            .verify(jwtToken)
    } catch (_: Exception) {
        null
    }
}

/**
 * Retrieves the id from the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the id.
 * @return The id if valid; null otherwise.
 */
fun getAccountId(token: DecodedJWT): Int? {
    return token.getClaim("id")?.asInt()
}

/**
 * Retrieves the id from the provided JWT token.
 *
 * @param token The JWT token from which to extract id.
 * @return The id if valid; null otherwise.
 */
fun getAccountId(token: JWTToken?): Int? {
    val jwtToken = token?.token
    if (jwtToken != null) {
        return try {
            val decodedToken = decodeToken(jwtToken)
            decodedToken?.getClaim("id")?.asInt()

        } catch (_: JWTVerificationException) {
            null
        }
    }
    return null
}

/**
 * Retrieves the employee ID from the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the employee ID.
 * @return The employee ID if valid; null otherwise.
 */
fun getEmployeeRole(token: DecodedJWT?): Int? {
    return token?.getClaim("roleCode")?.asInt()
}

/**
 * Retrieves the employee ID from the provided JWT token.
 *
 * @param token The JWT token from which to extract the employee ID.
 * @return The employee ID if valid; null otherwise.
 */
fun getEmployeeRole(token: JWTToken?): Int? {
    val jwtToken = token?.token
    if (jwtToken != null) {
        val decodedToken = decodeToken(jwtToken)
        return decodedToken?.getClaim("roleCode")?.asInt()
    }
    return null
}

/**
 * Retrieves the CustomerInfo from the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the CustomerInfo.
 * @return The CustomerInfo if valid; null otherwise.
 */
fun getCustomerInfo(token: DecodedJWT?): CustomerInfo? {
    if (token != null) {
        val id = token.getClaim("id")?.asInt()
        val name = token.getClaim("name").toString()
        val surname = token.getClaim("surname").toString()
        val phone = token.getClaim("phone").toString()
        val email = token.getClaim("email").toString()
        return if (id != null)
            CustomerInfo(id, name, surname, phone, "", email, false)
        else
            null
    }
    return null
}

/**
 * Retrieves the CustomerInfo from the provided JWT token.
 *
 * @param token The JWT token from which to extract theCustomerInfo.
 * @return The CustomerInfo if valid; null otherwise.
 */
fun getCustomerInfo(token: JWTToken?): CustomerInfo? {
    val jwtToken = token?.token
    if (jwtToken != null) {
        val decodedToken = decodeToken(jwtToken)
        val id = decodedToken?.getClaim("id")?.asInt()
        val name = decodedToken?.getClaim("name").toString()
        val surname = decodedToken?.getClaim("surname").toString()
        val phone = decodedToken?.getClaim("phone").toString()
        val email = decodedToken?.getClaim("email").toString()
        return if (id != null)
            CustomerInfo(id, name, surname, phone, "", email, false)
        else
            null
    }
    return null
}

/**
 * Retrieves the GuardInfo from the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the GuardInfo.
 * @return The GuardInfo if valid; null otherwise.
 */
fun getGuardInfo(token: DecodedJWT?): GuardInfo? {
    if (token != null) {
        val id = token.getClaim("id")?.asInt()
        val name = token.getClaim("name").toString()
        val surname = token.getClaim("surname").toString()
        val phone = token.getClaim("phone").toString()
        val email = token.getClaim("email").toString()
        return if (id != null)
            GuardInfo(id, name, surname, phone, email, Guard.GuardStatus.UNAVAILABLE.status, "", false)
        else
            null
    }
    return null
}

/**
 * Retrieves the GuardInfo from the provided JWT token.
 *
 * @param token The JWT token from which to extract theGuardInfo.
 * @return The GuardInfo if valid; null otherwise.
 */
fun getGuardInfo(token: JWTToken?): GuardInfo? {
    val jwtToken = token?.token
    if (jwtToken != null) {
        val decodedToken = decodeToken(jwtToken)
        val id = decodedToken?.getClaim("id")?.asInt()
        val name = decodedToken?.getClaim("name").toString()
        val surname = decodedToken?.getClaim("surname").toString()
        val phone = decodedToken?.getClaim("phone").toString()
        val email = decodedToken?.getClaim("email").toString()
        return if (id != null)
            GuardInfo(id, name, surname, phone, email, Guard.GuardStatus.UNAVAILABLE.status, "", false)
        else
            null
    }
    return null
}

/**
 * Retrieves the expiration date of the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the expiration date.
 * @return The expiration date if valid; null otherwise.
 */
fun getTokenExpirationDate(token: DecodedJWT): Date? {
    return token.expiresAt
}

/**
 * Retrieves the expiration date of the provided JWT token.
 *
 * @param token The JWT token from which to extract the expiration date.
 * @return The expiration date if valid; null otherwise.
 */
fun getTokenExpirationDate(token: JWTToken?): Date? {
    val jwtToken = token?.token
    if (jwtToken != null) {
        val decodedToken = decodeToken(jwtToken)
        return decodedToken?.expiresAt
    }
    return null
}
