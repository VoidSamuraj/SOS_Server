package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.hsts.*

fun Application.configureHTTPS() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    //inform websites to use https
    install(HSTS) {
        includeSubDomains = true
        preload = true
    }

}
