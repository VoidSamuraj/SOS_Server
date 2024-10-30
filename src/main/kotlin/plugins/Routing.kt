package plugins

import routes.mainRoutes
import routes.staticRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import routes.actionRoutes
import routes.authRoutes
import routes.databaseRoutes

fun Application.configureRouting() {

    staticRoutes()
    routing {
        mainRoutes()
        authRoutes()
        databaseRoutes()
        actionRoutes()
    }
}
