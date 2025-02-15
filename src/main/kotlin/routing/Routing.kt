package com.dn0ne.routing

import com.dn0ne.service.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userService: UserService,
    jwtService: JwtService,
    verificationService: VerificationService,
    accountService: AccountService,
    transactionService: TransactionService
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

            route("/api/transaction") {
                transactionRoute(
                    transactionService,
                    accountService
                )
            }
        }
    }
}