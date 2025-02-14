package com.dn0ne.routing

import com.dn0ne.model.account.Account
import com.dn0ne.routing.response.AccountResponse
import com.dn0ne.service.AccountService
import com.dn0ne.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.accountRoute(
    accountService: AccountService,
    userService: UserService,
) {
    post("/open") {
        val username = extractPrincipalUsername(call)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        val user = userService.findByUsername(username)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        val account = Account(
            id = UUID.randomUUID(),
            holderId = user.id,
            isActive = true,
        )

        accountService.openAccount(account)?.let {
            return@post call.respond(HttpStatusCode.Created)
        } ?: return@post call.respond(HttpStatusCode.InternalServerError)
    }

    get {
        val username = extractPrincipalUsername(call)
            ?: return@get call.respond(HttpStatusCode.Forbidden)

        val user = userService.findByUsername(username)
            ?: return@get call.respond(HttpStatusCode.Forbidden)

        val response = accountService.findByHolderId(user.id.toString()).map {
            AccountResponse(
                id = it.id.toString(),
                isActive = it.isActive,
                balance = accountService.getBalance(it)
                    ?: return@get call.respond(HttpStatusCode.InternalServerError)
            )
        }

        call.respond(HttpStatusCode.OK, response)
    }

    post("/close/{id}") {
        val accountId = call.parameters["id"]
            ?: return@post call.respond(HttpStatusCode.BadRequest)

        val account = accountService.findById(accountId)
            ?: return@post call.respond(HttpStatusCode.NotFound)

        val username = extractPrincipalUsername(call)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        val user = userService.findByUsername(username)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        if (account.holderId != user.id) {
            return@post call.respond(HttpStatusCode.Forbidden)
        }

        accountService.closeAccount(accountId)?.let {
            return@post call.respond(HttpStatusCode.OK)
        } ?: return@post call.respond(HttpStatusCode.InternalServerError)
    }
}

fun extractPrincipalUsername(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()