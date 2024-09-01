package routes

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.staticRoutes(){
    routing {
        staticResources("icons/","files/icons")
        staticResources("/static","react/build/static")
    }
}