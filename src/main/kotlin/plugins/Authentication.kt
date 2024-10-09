package plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dao.DaoMethods
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import routes.scheduleTokenCleanup
import security.Keys
import security.Keys.ISS


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
                    .withIssuer(ISS)
                    .build())

            validate { credential ->
                val login=credential.payload.getClaim("login").asString()
                val password=credential.payload.getClaim("password").asString()
                val audience = credential.payload.audience?.firstOrNull()
                when(audience){
                    "employee"->{
                        val employee= DaoMethods.getEmployee(login,password)
                        if (login.isNotEmpty() && password.isNotEmpty() && employee.second != null) {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                    "customer"->{
                        val employee= DaoMethods.getCustomer(login,password)
                        if (login.isNotEmpty() && password.isNotEmpty() && employee.second != null) {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                    "guard"->{
                        val employee= DaoMethods.getGuard(login,password)
                        if (login.isNotEmpty() && password.isNotEmpty() && employee.second != null) {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                    else -> null
                }


            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    scheduleTokenCleanup()
}
