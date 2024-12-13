package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.entity.Employee
import java.io.File

fun Route.mainRoutes(){
    get("/map") {
        checkUserPermission(
            roles = listOf(Employee.Role.ADMIN, Employee.Role.DISPATCHER),
            onSuccess = {
            call.respondFile(File("src/main/resources/react/build/index.html"))
        })
    }
    get("/login") {
        checkUserPermission(onSuccess = {role->
            when(role){
                Employee.Role.ADMIN, Employee.Role.DISPATCHER ->
                    call.respondRedirect("/map")
                Employee.Role.MANAGER->
                    call.respondRedirect("/administration")
                else ->
                    call.respondFile(File("src/main/resources/react/build/login.html"))
            }
        },
            onFailure = {
                call.respondFile(File("src/main/resources/react/build/login.html"))
            }
        )
    }
    get("/administration") {
        checkUserPermission(
            roles = listOf(Employee.Role.ADMIN, Employee.Role.MANAGER),
            onSuccess = {
            call.respondFile(File("src/main/resources/react/build/administration.html"))
        })

    }
    get("/reset-password"){
        call.respondFile(File("src/main/resources/react/build/resetPassword.html"))
    }

    get("{...}") {
        call.respondRedirect("/login")
    }

}