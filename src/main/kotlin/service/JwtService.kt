package com.dn0ne.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.dn0ne.model.user.isActive
import com.dn0ne.routing.request.LoginRequest
import com.dn0ne.util.verify
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import org.slf4j.LoggerFactory
import java.util.Date

class JwtService(
    private val application: Application,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    private val secret = getConfigProperty("jwt.secret")
    private val issuer = getConfigProperty("jwt.issuer")
    private val audience = getConfigProperty("jwt.audience")
    val realm = getConfigProperty("jwt.realm")

    val jwtVerifier: JWTVerifier =
        JWT.require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

    suspend fun createToken(loginRequest: LoginRequest): String? {
        logger.info("Authenticating ${loginRequest.username}")

        val foundUser = userService.findByUsername(loginRequest.username)
            ?.takeIf { it.isActive() }

        return if (foundUser != null && loginRequest.password.verify(foundUser.password)) {
            logger.info("Authenticated user ${foundUser.username}")
            JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", foundUser.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
                .sign(Algorithm.HMAC256(secret))
        } else {
            logger.info("Authentication denied for ${loginRequest.username}")
            null
        }
    }

    suspend fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val username = extractUsername(credential)
        val foundUser = username?.let {
            userService.findByUsername(it)
        }

        return foundUser?.let {
            if (audienceMatches(credential)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(audience)

    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()

    private fun getConfigProperty(path: String) =
        application.environment.config.property(path).getString()
}