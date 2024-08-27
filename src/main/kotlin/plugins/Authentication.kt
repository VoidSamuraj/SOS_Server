package plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import security.HashPassword
import security.JWTToken
import security.Keys
import java.util.*
/*
fun Application.configureWorkerAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(Keys.JWTSecret))
                    .build())

            validate { credential ->
                val login=credential.payload.getClaim("login").asString()
                val password=credential.payload.getClaim("password").asString()
                val dbPassword=dao.getUserPassword(login=login)
                if (login.isNotEmpty() && !dbPassword.isNullOrEmpty() && password.isNotEmpty() && HashPassword.comparePasswords(password,dbPassword)) {
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
    val id = getUserId(token)
    if(expireTime!=null&&expireTime.after(Date())&&id!=null&& dao.getUser(id)!=null){
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
fun getUserId(token: JWTToken?):Int?{
    val jwtToken = token?.token
    if(jwtToken!=null){
        return try {
            val decodedToken = decodeToken(jwtToken)
            decodedToken.getClaim("id").asInt()

        } catch (e: JWTVerificationException) {
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

        } catch (e: JWTVerificationException) {
            null
        }
    }
    return null
}
*/