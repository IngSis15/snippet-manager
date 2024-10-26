package edu.ingsis.snippetmanager.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtTokenUtil {
    private val secret = "secret"

    fun createToken(scope: String, subject: String = "test-user"): String{
        return JWT.create()
            .withSubject(subject)
            .withClaim("scope", scope)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 hour expiration
            .sign(Algorithm.HMAC256(secret))

    }
}