package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.mainRoutes(){
    get("/map") {
        checkUserPermission(onSuccess = {
            call.respondFile(File("src/main/resources/react/build/index.html"))
        })
    }
    get("/login") {
        checkUserPermission(onSuccess = {
            call.respondRedirect("/map")
        },
            onFailure = {
                call.respondFile(File("src/main/resources/react/build/login.html"))
            }
        )
    }
    get("/administration") {
        checkUserPermission(onSuccess = {
            call.respondFile(File("src/main/resources/react/build/administration.html"))
        })

    }
    get("/reset-password"){
        call.respondFile(File("src/main/resources/react/build/remindPassword.html"))
    }

    get("{...}") {
        call.respondRedirect("/login")
    }

}