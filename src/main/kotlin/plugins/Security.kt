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


/**
 * Generates a random login string of specified length.
 *
 * The login consists of uppercase letters, lowercase letters, and digits.
 * The default length is set to 8 characters, but it can be customized by passing
 * a different length as a parameter.
 *
 * @param length The length of the generated login string (default is 8).
 * @return A random login string consisting of allowed characters.
 */
fun generateRandomLogin(length: Int = 8): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}


/**
 * Generates a random password string that meets complexity requirements.
 *
 * The password will contain at least one lowercase letter, one uppercase letter,
 * one digit, and one special character. Additionally, the password will be at least
 * 8 characters long. The remaining characters are filled in with random selections
 * from a predefined set of allowed characters, which includes uppercase and lowercase
 * letters, digits, and special characters.
 *
 * @return A random password string that satisfies the complexity requirements.
 */
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

/**
 * Sanitize the provided HTML string to prevent XSS attacks.
 * This function removes all tags and attributes from the input HTML.
 *
 * @param html The HTML string to sanitize.
 * @return A sanitized version of the HTML string.
 */
fun sanitizeHtml(html: String): String {
    return Jsoup.clean(html, Safelist.none())
}

/**
 * Check if the provided username is valid.
 * A valid username must be between 3 and 40 characters long.
 *
 * @param username The username to validate.
 * @return True if the username is valid; otherwise, false.
 */
fun isUsernameValid(username: String): Boolean {
    return username.length in 3..40
}

/**
 * Check if the provided login username is valid.
 * A valid login username must be between 3 and 20 characters long.
 *
 * @param username The login username to validate.
 * @return True if the login username is valid; otherwise, false.
 */
fun isLoginValid(username: String): Boolean {
    return username.length in 3..20
}

/**
 * Check if the provided password is valid.
 * A valid password must be at least 8 characters long and contain at least one lowercase letter,
 * one uppercase letter, one digit, and one special character.
 *
 * @param password The password to validate.
 * @return True if the password is valid; otherwise, false.
 */
fun isPasswordValid(password: String): Boolean {
    val regex = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$""".toRegex()
    return regex.matches(password)
}


/**
 * Check if the provided phone number is valid.
 * A valid phone number can optionally start with a '+' and must be between 10 and 13 digits long.
 *
 * @param phoneNumber The phone number to validate.
 * @return True if the phone number is valid; otherwise, false.
 */
fun isPhoneValid(phoneNumber: String): Boolean {
    val regex = "^[+]?[0-9]{10,13}$".toRegex()
    return regex.matches(phoneNumber)
}

/**
 * Check if the provided email address is valid.
 * A valid email address follows the standard format of local-part@domain.
 *
 * @param email The email address to validate.
 * @return True if the email address is valid; otherwise, false.
 */
fun isEmailValid(email: String): Boolean {
    val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    val pattern = Pattern.compile(emailRegex)
    return pattern.matcher(email).matches()
}

/**
 * Check if the provided PESEL number is valid.
 * A valid PESEL must be exactly 11 digits long and pass a control sum validation.
 *
 * @param pesel The PESEL number to validate.
 * @return True if the PESEL number is valid; otherwise, false.
 */
fun isPeselValid(pesel: String): Boolean {
    if (pesel.length != 11 || !pesel.all { it.isDigit() }) {
        return false
    }

    val digits = pesel.map { it.toString().toInt() }

    val weights = intArrayOf(1, 3, 7, 9, 1, 3, 7, 9, 1, 3)

    // control sum
    val sum = digits.zip(weights.asIterable()).sumOf { (digit, weight) -> digit * weight }

    // compare control sum with last number
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