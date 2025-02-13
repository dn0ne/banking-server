package com.dn0ne.routing

import com.dn0ne.model.user.User
import com.dn0ne.model.user.isVerified
import com.dn0ne.routing.request.RegisterRequest
import com.dn0ne.service.UserService
import com.dn0ne.service.VerificationService
import com.dn0ne.util.hash
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.registerRoute(
    userService: UserService,
    verificationService: VerificationService
) {
    post {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        } catch (_: ContentTransformationException) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val createdUser = userService.register(
            user = registerRequest.copy(
                password = registerRequest.password.hash()
            ).toModel()
        ) ?: run {
            val existingUser = userService.findByUsername(registerRequest.username)
            if (existingUser?.isVerified() == false) {
                verificationService.sendVerificationEmail(existingUser)
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            return@post call.respond(HttpStatusCode.Conflict)
        }

        verificationService.sendVerificationEmail(createdUser)

        call.respond(HttpStatusCode.Created)
    }
}

private fun RegisterRequest.toModel(): User =
    User(
        id = UUID.randomUUID(),
        username = this.username,
        password = this.password,
        state = User.State.VerificationRequired
    )
