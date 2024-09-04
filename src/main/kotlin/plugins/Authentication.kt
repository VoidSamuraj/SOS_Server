package plugins

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
import security.HashPassword
import security.JWTToken
import security.Keys
import java.util.*

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
                if (login.isNotEmpty() && password.isNotEmpty() && employee!=null && HashPassword.comparePasswords(password,employee.password)) {
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
suspend fun checkPermission(token:JWTToken?, onSuccess: suspend ()->Unit,onFailure:suspend ()->Unit){
    val expireTime= getTokenExpirationDate(token)
    val id = getEmployeeId(token)
    if(expireTime!=null&&expireTime.after(Date())&&id!=null&& DaoMethods.getEmployee(id)!=null){
        onSuccess()
    }else{
        onFailure()
    }
}
fun decodeToken(jwtToken:String?):DecodedJWT{
    return JWT.require(Algorithm.HMAC256(Keys.JWTSecret))
        .build()
        .verify(jwtToken)
}
fun getEmployeeId(token: JWTToken?):Int?{
    val jwtToken = token?.token
    if(jwtToken!=null){
        return try {
            val decodedToken = decodeToken(jwtToken)
            decodedToken.getClaim("id").asInt()

        } catch (_: JWTVerificationException) {
            null
        }
    }
    return null
}
fun getTokenExpirationDate(token: JWTToken?):Date?{
    val jwtToken = token?.token
    if(jwtToken!=null){
        return try {
            val decodedToken = decodeToken(jwtToken)
            decodedToken.expiresAt

        } catch (_: JWTVerificationException) {
            null
        }
    }
    return null
}
