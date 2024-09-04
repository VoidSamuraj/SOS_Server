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

            /*
            val googleKey = Keys.googleApiKey
            val mapId = Keys.googleMapId

            // Przygotowanie modelu danych do szablonu FreeMarker
            val model = mapOf(
                "map_id" to mapId,
                "google_api_key" to googleKey,
                //"expiration" to getTokenExpirationDate(call.sessions.get("TOKEN") as MyToken?)?.time,
                //"lifeTime" to jwtExpirationSeconds
            )

            call.respondTemplate(template = "index.ftl", model =model)
            */    call.respondFile(File("src/main/resources/react/build/index.html"))

              /*  },
                onFailure = { call.respondRedirect("/user/login")}
            )*/

        }

    get("{...}") {
        call.respondRedirect("/")
    }

}