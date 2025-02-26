package com.dn0ne.routing

import com.dn0ne.model.user.User
import com.dn0ne.routing.request.VerificationRequest
import com.dn0ne.service.UserService
import com.dn0ne.service.VerificationService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.verifyRoute(
    verificationService: VerificationService,
    userService: UserService,
) {
    post {
        val verificationRequest = try {
            call.receive<VerificationRequest>()
        } catch (e: ContentTransformationException) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val verifiedUser = verificationService.verifyEmail(verificationRequest)
            ?: return@post call.respond(HttpStatusCode.BadRequest)

        val isUserFound = userService.verify(verifiedUser)
        if (isUserFound) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}