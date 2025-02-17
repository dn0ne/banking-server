package com.dn0ne.routing

import com.dn0ne.model.account.Account
import com.dn0ne.routing.response.AccountResponse
import com.dn0ne.service.AccountResult
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

        return@post when (val result = accountService.openAccount(account)) {
            AccountResult.Success -> call.respond(HttpStatusCode.Created)
            AccountResult.HolderNotFound -> call.respond(HttpStatusCode.Forbidden)
            else -> call.respond(
                HttpStatusCode.Conflict,
                mapOf(
                    "error" to result
                )
            )
        }
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

        val username = extractPrincipalUsername(call)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        val isAccountHolder = accountService.checkHolder(accountId, username)
        if (!isAccountHolder) {
            return@post call.respond(HttpStatusCode.Forbidden)
        }

        return@post when (val result = accountService.closeAccount(accountId)) {
            AccountResult.Success -> call.respond(HttpStatusCode.OK)
            else -> call.respond(
                HttpStatusCode.Conflict,
                mapOf(
                    "error" to result
                )
            )
        }
    }

    post("/reopen/{id}") {
        val accountId = call.parameters["id"]
            ?: return@post call.respond(HttpStatusCode.BadRequest)

        val username = extractPrincipalUsername(call)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        val isAccountHolder = accountService.checkHolder(accountId, username)
        if (!isAccountHolder) {
            return@post call.respond(HttpStatusCode.Forbidden)
        }

        return@post when (val result = accountService.reopenAccount(accountId)) {
            AccountResult.Success -> call.respond(HttpStatusCode.OK)
            else -> call.respond(
                HttpStatusCode.Conflict,
                mapOf(
                    "error" to result
                )
            )
        }
    }
}

fun extractPrincipalUsername(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()