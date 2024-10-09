package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import dao.DaoMethods
import jwtExpirationSeconds
import kotlinx.serialization.Serializable
import security.Keys.ISS
import java.util.Date

@Serializable
data class JWTToken(val token:String)



/**
 * Checks the validity of the provided JWT token and determines
 * if the associated employee has valid permissions.
 *
 * @param token The JWT token to validate.
 * @param onSuccess A suspend function to execute if the token is valid and the employee exists.
 * @param onFailure A suspend function to execute if the token is invalid or expired.
 */
suspend fun checkPermission(token:JWTToken?, onSuccess: suspend ()->Unit,onFailure:suspend ()->Unit){

    if (token == null) {
        onFailure()
        return
    }

    val decodedToken=decodeToken(token.token)
    //token verification

    if(decodedToken == null) {
        onFailure()
        return
    }

    //check token expiration
    val expireTime = getTokenExpirationDate(decodedToken)
    if (expireTime == null || expireTime.before(Date(System.currentTimeMillis()))) {
        onFailure()
        return
    }

    val id = getAccountId(decodedToken)
    if (id != null && DaoMethods.getEmployee(id) != null) {
        onSuccess()
    } else {
        onFailure()
    }
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
 * @return A Pair containing JWTToken and expiration date.
 */
fun createToken(customer: Customer): Pair<JWTToken, Long>{
    val token = JWT.create()
        .withIssuer(ISS)
        .withAudience("customer")
        .withClaim("id", customer.id)
        .withClaim("login", customer.login)
        .withClaim("name", customer.name)
        .withClaim("surname", customer.surname)
        .withClaim("phone", customer.phone)
        .withClaim("email", customer.email)
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return Pair(JWTToken(token),Long.MAX_VALUE)
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
 * @return A Pair containing JWTToken and expiration date.
 */
fun createToken(guard: Guard): Pair<JWTToken, Long>{
    val token = JWT.create()
        .withIssuer(ISS)
        .withAudience("guard")
        .withClaim("id", guard.id)
        .withClaim("login", guard.login)
        .withClaim("name", guard.name)
        .withClaim("surname", guard.surname)
        .withClaim("phone", guard.phone)
        .withClaim("email", guard.email)
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return Pair(JWTToken(token),Long.MAX_VALUE)
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
fun createToken(employee: Employee): Pair<JWTToken, Long>{
    val tokenExp = Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000)
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
        .withExpiresAt(tokenExp)
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return Pair(JWTToken(token),tokenExp.time)
}

/**
 * Decodes a JWT token and verifies its signature.
 *
 * @param jwtToken The JWT token to decode.
 * @return A DecodedJWT object containing the claims of the token, null if is not valid.
 */
fun decodeToken(jwtToken:String?):DecodedJWT?{
    return try {
        JWT.require(Algorithm.HMAC256(Keys.JWTSecret))
            .build()
            .verify(jwtToken)
    }catch(_: Exception){
        null
    }
}

/**
 * Retrieves the id from the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the id.
 * @return The id if valid; null otherwise.
 */
fun getAccountId(token: DecodedJWT):Int?{
    return token.getClaim("id")?.asInt()
}

/**
 * Retrieves the id from the provided JWT token.
 *
 * @param token The JWT token from which to extract id.
 * @return The id if valid; null otherwise.
 */
fun getAccountId(token: JWTToken?):Int?{
    val jwtToken = token?.token
    if(jwtToken!=null){
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
fun getEmployeeRole(token: DecodedJWT?):Int?{
    return token?.getClaim("roleCode")?.asInt()
}

/**
 * Retrieves the employee ID from the provided JWT token.
 *
 * @param token The JWT token from which to extract the employee ID.
 * @return The employee ID if valid; null otherwise.
 */
fun getEmployeeRole(token: JWTToken?):Int?{
    val jwtToken = token?.token
    if(jwtToken!=null){
        val decodedToken = decodeToken(jwtToken)
        return decodedToken?.getClaim("roleCode")?.asInt()
    }
    return null
}

/**
 * Retrieves the expiration date of the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the expiration date.
 * @return The expiration date if valid; null otherwise.
 */
fun getTokenExpirationDate(token: DecodedJWT):Date?{
    return token.expiresAt
}

/**
 * Retrieves the expiration date of the provided JWT token.
 *
 * @param token The JWT token from which to extract the expiration date.
 * @return The expiration date if valid; null otherwise.
 */
fun getTokenExpirationDate(token: JWTToken?):Date?{
    val jwtToken = token?.token
    if(jwtToken!=null){
        val decodedToken = decodeToken(jwtToken)
        return decodedToken?.expiresAt
    }
    return null
}
