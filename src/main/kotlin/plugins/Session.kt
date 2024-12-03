package plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import jwtExpirationMilliSeconds
import security.JWTToken
import security.Keys
import java.io.File
fun Application.configureSession() {

    install(Sessions) {
        cookie<JWTToken>("userToken", directorySessionStorage(File("build/.sessions"))) {
            transform(SessionTransportTransformerEncrypt(Keys.EncryptKey, Keys.SignKey))
            //to enable using cookies by js
            cookie.httpOnly=true
            cookie.secure=true
            cookie.maxAgeInSeconds =jwtExpirationMilliSeconds/1000
        }
    }

}


