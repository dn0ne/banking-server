package com.dn0ne.routing

import com.dn0ne.routing.request.LoginRequest
import com.dn0ne.service.JwtService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.loginRoute(jwtService: JwtService) {
    post {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: ContentTransformationException) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val token = jwtService.createToken(loginRequest)

        token?.let {
            call.respond(hashMapOf("token" to it))
        } ?: call.respond(HttpStatusCode.Unauthorized)
    }
}