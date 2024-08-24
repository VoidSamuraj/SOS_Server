package example.com.plugins

import example.com.routes.mainRoutes
import example.com.routes.staticRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    staticRoutes()
    routing {
        mainRoutes()
    }
}
