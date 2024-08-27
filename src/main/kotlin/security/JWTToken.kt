package security

import kotlinx.serialization.Serializable

@Serializable
data class JWTToken(val token:String)