package com.dn0ne.routing

import com.dn0ne.service.AccountService
import com.dn0ne.service.JwtService
import com.dn0ne.service.UserService
import com.dn0ne.service.VerificationService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userService: UserService,
    jwtService: JwtService,
    verificationService: VerificationService,
    accountService: AccountService,
) {
    routing {
        route("/api/register") {
            registerRoute(userService, verificationService)
        }

        route("/api/verify") {
            verifyRoute(verificationService, userService)
        }

        route("/api/login") {
            loginRoute(jwtService)
        }

        authenticate {
            route("/api/account") {
                accountRoute(accountService, userService)
            }
        }
    }
}