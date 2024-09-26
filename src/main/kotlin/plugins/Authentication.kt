package plugins

import Employee
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import dao.DaoMethods
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import jwtExpirationSeconds
import security.JWTToken
import security.Keys
import java.util.*


/**
 * Configures JWT authentication for the Ktor application.
 *
 * This function sets up the JWT authentication mechanism using the `Authentication` feature in Ktor.
 * It installs a JWT provider named "auth-jwt" which verifies the incoming JWT tokens and validates
 * the claims contained within the token. The following claims are checked:
 * - `login`: The login of the employee.
 * - `password`: The password of the employee.
 *
 * If the provided login and password are valid and correspond to an existing employee, the JWTPrincipal
 * is returned; otherwise, it returns null.
 *
 * In case of an invalid or expired token, a 401 Unauthorized response is sent back to the client
 * with a relevant message.
 */
fun Application.configureWorkerAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(Keys.JWTSecret))
                    .build())

            validate { credential ->
                val login=credential.payload.getClaim("login").asString()
                val password=credential.payload.getClaim("password").asString()
                val employee= DaoMethods.getEmployee(login,password)
                if (login.isNotEmpty() && password.isNotEmpty() && employee.second!=null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

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

    val id = getEmployeeId(decodedToken)
    if (id != null && DaoMethods.getEmployee(id) != null) {
        onSuccess()
    } else {
        onFailure()
    }
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
 * @return A JWTToken containing the generated token.
 */
fun createToken(employee: Employee): JWTToken{
    val token = JWT.create()
        .withClaim("id", employee.id)
        .withClaim("login", employee.login)
        .withClaim("name", employee.name)
        .withClaim("surname", employee.surname)
        .withClaim("phone", employee.phone)
        .withClaim("email", employee.email)
        .withClaim("roleCode", employee.roleCode.toInt())
        .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000))
        .sign(Algorithm.HMAC256(Keys.JWTSecret))
    return JWTToken(token)
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
 * Retrieves the employee role code from the provided DecodedJWT token.
 *
 * @param token The DecodedJWT token from which to extract the employee role cod.
 * @return The employee role code if valid; null otherwise.
 */
fun getEmployeeId(token: DecodedJWT):Int?{
    return token.getClaim("id")?.asInt()
}

/**
 * Retrieves the employee role code from the provided JWT token.
 *
 * @param token The JWT token from which to extract the employee role cod.
 * @return The employee role code if valid; null otherwise.
 */
fun getEmployeeId(token: JWTToken?):Int?{
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
