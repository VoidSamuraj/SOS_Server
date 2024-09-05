package security

import org.mindrot.jbcrypt.BCrypt

object HashPassword {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun comparePasswords(passwordToCompare: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(passwordToCompare, hashedPassword)
        }catch(_: Exception){
            false
        }
    }

}