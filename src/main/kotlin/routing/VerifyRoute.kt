package com.dn0ne.routing

import com.dn0ne.service.UserService
import com.dn0ne.service.VerificationService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.verifyRoute(
    verificationService: VerificationService,
    userService: UserService,
) {
    get("/{token}") {
        val token = call.parameters["token"]
            ?: return@get call.respond(HttpStatusCode.BadRequest)

        val verifiedUser = verificationService.verifyEmail(token)
            ?: return@get call.respond(HttpStatusCode.BadRequest)

        val isUserFound = userService.verify(verifiedUser)
        if (isUserFound) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}