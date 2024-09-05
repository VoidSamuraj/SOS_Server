package routes

import Employee
import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.util.getOrFail
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import jwtExpirationSeconds
import plugins.checkPermission
import plugins.createToken
import plugins.decodeToken
import plugins.getTokenExpirationDate
import security.JWTToken
import java.util.Date

fun Route.authRoutes(){
    var mToken: JWTToken?=null
    var errorMessage:String=""

    suspend fun PipelineContext<Unit, ApplicationCall>.checkUserPermission(onSuccess:suspend ()->Unit){
        val token=call.sessions.get("userToken")as JWTToken?
        checkPermission(token = token,
            onSuccess = {
                mToken=token
                onSuccess()
            },
            onFailure = {
                call.respondRedirect("/")
            })
    }

    fun PipelineContext<Unit,ApplicationCall>.generateToken(employee: Employee): JWTToken?{
        mToken = createToken(employee)
        call.sessions.set(mToken)
        return mToken!!
    }

    suspend fun PipelineContext<Unit,ApplicationCall>.onAuthenticate(employee: Employee){
        getTokenExpirationDate(generateToken(employee)).let { it1 -> call.attributes.put(AttributeKey("exp"), it1?.time.toString()) }
        val responseObject = mapOf("message" to "Success", "exp" to Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000).time.toString())
        call.respond(HttpStatusCode.OK, responseObject)
    }
    suspend fun PipelineContext<Unit,ApplicationCall>.onAuthError(){
        call.respondRedirect("/")
    }

    route("/employee"){
        post("/login") {
            val formParameters = call.receiveParameters()
            val login = formParameters.getOrFail("login")
            val password = formParameters.getOrFail("password")
            val employee= DaoMethods.getEmployee(login, password)
            println("ERRROORO "+DaoMethods.getAllEmployees(1,10))
            if(employee.second!=null)
                onAuthenticate(employee.second!!)
            else {
                errorMessage =employee.first
                onAuthError()
            }
        }
        post("/refresh-token-expiration"){
            val token=call.sessions.get("userToken")as JWTToken?
            checkPermission(token = token,
                onSuccess = {
                    val decodedToken= decodeToken(token?.token)
                    val employee=Employee(
                        decodedToken.claims["id"]!!.asInt(),
                        decodedToken.claims["login"]!!.asString(),
                        "",
                        decodedToken.claims["name"]!!.asString(),
                        decodedToken.claims["surname"]!!.asString(),
                        decodedToken.claims["phone"]!!.asString(),
                        decodedToken.claims["roleCode"]!!.asInt().toShort(),
                        false
                        )
                    getTokenExpirationDate(generateToken(employee))?.time?.let { it1 -> call.attributes.put(AttributeKey("exp"), it1.toString()) }

                    call.respond(HttpStatusCode.OK, "Success")
                },
                onFailure = { call.respondRedirect("/user/login")}
            )
        }
        post("/logout") {
            call.sessions.clear("userToken")
            call.respond(HttpStatusCode.OK, "Success")
        }
        post("/register") {
            val formParameters = call.receiveParameters()
            val login = formParameters.getOrFail("login")
            val password = formParameters.getOrFail("password")
            // TODO
            /*
            val employee= DaoMethods.addEmployee(login,password,)
            if(employee.first)
                onAuthenticate(employee.third!!)
            else {
                errorMessage ="Register error"
                onAuthError()
            }
            */
            //TEST
            var employee= DaoMethods.getEmployee(1)
                onAuthenticate(employee!!)

            }
        }
    }

