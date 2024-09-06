package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*
fun Application.configureHTTP() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
        header("Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none';")
    }
}
