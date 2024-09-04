package plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import jwtExpirationSeconds
import security.JWTToken
import security.Keys
import java.io.File
fun Application.configureSession() {

    install(Sessions) {
        cookie<JWTToken>("userToken", directorySessionStorage(File("build/.sessions"))) {
            transform(SessionTransportTransformerEncrypt(Keys.EncryptKey, Keys.SignKey))
            //to enable using cookies by js
            cookie.httpOnly=false
            cookie.secure=true
            cookie.maxAgeInSeconds =jwtExpirationSeconds
        }
    }

}


