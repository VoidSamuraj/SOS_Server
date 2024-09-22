package plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.util.regex.Pattern

fun generateRandomLogin(length: Int = 8): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun generateRandomPassword(): String {
    val lowercase = ('a'..'z').random()
    val uppercase = ('A'..'Z').random()
    val digit = ('0'..'9').random()
    val specialChar = listOf('@', '$', '!', '%', '*', '?', '&').random()

    val allChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&"

    // Ensure that the password contains at least 8 characters by filling in with random characters.
    val remainingChars = (1..4)
        .map { allChars.random() }
        .joinToString("")

    // Shuffle and return the password to ensure randomness
    return listOf(lowercase, uppercase, digit, specialChar, remainingChars)
        .joinToString("")
        .toList()
        .shuffled()
        .joinToString("")
}


//TODO use this methods
fun sanitizeHtml(html: String): String {
    return Jsoup.clean(html, Safelist.none())
}
fun validateUsername(username: String): Boolean {
    return username.length in 3..20
}
fun validatePhoneNumber(phoneNumber: String): Boolean {
    val regex = "^[+]?[0-9]{10,13}$".toRegex()
    return regex.matches(phoneNumber)
}

fun validateEmail(email: String): Boolean {
    val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    val pattern = Pattern.compile(emailRegex)
    return pattern.matcher(email).matches()
}
fun isValidPESEL(pesel: String): Boolean {
    if (pesel.length != 11 || !pesel.all { it.isDigit() }) {
        return false
    }

    val digits = pesel.map { it.toString().toInt() }

    // Definicja wag
    val weights = intArrayOf(1, 3, 7, 9, 1, 3, 7, 9, 1, 3)

    // Obliczanie sumy kontrolnej
    val sum = digits.zip(weights.asIterable()).sumOf { (digit, weight) -> digit * weight }

    // Sprawdzenie, czy suma kontrolna jest zgodna z ostatnią cyfrą PESEL
    return sum % 10 == 0
}

/*
fun Application.configureSecurity() {

    data class MySession(val count: Int = 0)
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    authentication {
            oauth("auth-oauth-google") {
                urlProvider = { "http://localhost:8080/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = System.getenv("GOOGLE_CLIENT_ID"),
                        clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                        defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                    )
                }
                client = HttpClient(Apache)
            }
        }
    routing {
        get("/session/increment") {
                val session = call.sessions.get<MySession>() ?: MySession()
                call.sessions.set(session.copy(count = session.count + 1))
                call.respondText("Counter is ${session.count}. Refresh to increment.")
            }
        authenticate("auth-oauth-google") {
                    get("auth") { //login->auth
                        call.respondRedirect("/callback")
                    }
        
                    get("/callback") {
                        val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                        call.sessions.set(UserSession(principal?.accessToken.toString()))
                        call.respondRedirect("/hello")
                    }
                }
    }

}

class UserSession(accessToken: String)
*/