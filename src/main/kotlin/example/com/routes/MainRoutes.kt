package example.com.routes

import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mainRoutes(){
    get(){
        call.respondRedirect("/user/login")
    }
    route("/index"){
        get {
/*
            checkPermission(token = call.sessions.get("TOKEN")as MyToken?,
                onSuccess = {*/
                    call.respondTemplate(template = "index.ftl"/*, model= mapOf("expiration" to getTokenExpirationDate(call.sessions.get("TOKEN")as MyToken?)?.time,"lifeTime" to jwtExpirationSeconds)*/)
              /*  },
                onFailure = { call.respondRedirect("/user/login")}
            )*/

        }
    }
}