package routes

import Employee
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dao.DaoMethods
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
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
import plugins.decodeToken
import plugins.getTokenExpirationDate
import security.HashPassword
import security.JWTToken
import security.Keys
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
        val token = JWT.create()
            .withClaim("id", employee.id)
            .withClaim("login", employee.login)
           // .withClaim("password", employee.password)
            .withClaim("name", employee.name)
            .withClaim("surname", employee.surname)
            .withClaim("phone", employee.phone)
            .withClaim("roleCode", employee.roleCode.toInt())
            .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000))
            .sign(Algorithm.HMAC256(Keys.JWTSecret))
        mToken = JWTToken(token)
        call.sessions.set(mToken)
        return mToken!!
    }

    suspend fun PipelineContext<Unit,ApplicationCall>.onAuthenticate(employee: Employee){
        generateToken(employee)
        Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000).time.let { it1 -> call.attributes.put(AttributeKey("exp"), it1.toString()) }
        val responseObject = mapOf("message" to "Success", "exp" to Date(System.currentTimeMillis() + jwtExpirationSeconds * 1000).time.toString())
        call.respond(HttpStatusCode.OK, responseObject)
        //call.respond(HttpStatusCode.OK, "Success")
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
            if(employee!=null && HashPassword.comparePasswords(password,employee.password))
                onAuthenticate(employee)
            else {
                errorMessage ="Login error"
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
            call.respondText("Logged out successfully")
        }
        post("/register") {
            val formParameters = call.receiveParameters()
            val login = formParameters.getOrFail("login")
            val password = HashPassword.hashPassword(formParameters.getOrFail("password"))
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
            val employee= DaoMethods.getEmployee(1)
                onAuthenticate(employee!!)

            }
        }
    }

