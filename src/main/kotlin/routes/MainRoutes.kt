package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.mainRoutes(){
        get {
/*
            checkPermission(token = call.sessions.get("TOKEN")as MyToken?,
                onSuccess = {*/


          call.respondFile(File("src/main/resources/react/build/index.html"))

              /*  },
                onFailure = { call.respondRedirect("/user/login")}
            )*/

        }

    get("{...}") {
        call.respondRedirect("/")
    }

}