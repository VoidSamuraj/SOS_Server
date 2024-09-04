package plugins

import routes.mainRoutes
import routes.staticRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import routes.authRoutes

fun Application.configureRouting() {

    staticRoutes()
    routing {
        mainRoutes()
        authRoutes()
    }
}
