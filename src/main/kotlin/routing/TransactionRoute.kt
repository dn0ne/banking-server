package com.dn0ne.routing

import com.dn0ne.model.Transaction
import com.dn0ne.repository.table.nowMicros
import com.dn0ne.routing.request.TransactionRequest
import com.dn0ne.routing.request.verify
import com.dn0ne.routing.response.TransactionResponse
import com.dn0ne.service.AccountService
import com.dn0ne.service.TransactionService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.*

fun Route.transactionRoute(
    transactionService: TransactionService,
    accountService: AccountService
) {
    get("/{id}") {
        val accountId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest)

        val username = extractPrincipalUsername(call)
            ?: return@get call.respond(HttpStatusCode.Forbidden)

        val isAccountHolder = accountService.checkHolder(accountId, username)
        if (!isAccountHolder) {
            return@get call.respond(HttpStatusCode.Forbidden)
        }

        val transactions = transactionService.findByAccountId(accountId).map {
            it.toResponse()
        }
        call.respond(HttpStatusCode.OK, transactions)
    }

    post {
        val transactionRequest = try {
            call.receive<TransactionRequest>().takeIf { it.verify() }
                ?: return@post call.respond(HttpStatusCode.BadRequest)
        } catch (e: ContentTransformationException) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val transaction = try {
            transactionRequest.toModel()
        } catch (e: IllegalArgumentException) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val isAccepted = transactionService.processTransaction(transaction)
        if (isAccepted) {
            return@post call.respond(HttpStatusCode.OK)
        } else return@post call.respond(HttpStatusCode.BadRequest)
    }
}

private fun Transaction.toResponse(): TransactionResponse =
    TransactionResponse(
        id = id.toString(),
        fromAccountId = fromAccountId.toString(),
        toAccountId = toAccountId.toString(),
        amount = amount,
        createdAt = createdAt
    )

private fun TransactionRequest.toModel(): Transaction =
    Transaction(
        id = UUID.randomUUID(),
        fromAccountId = UUID.fromString(fromAccountId),
        toAccountId = UUID.fromString(toAccountId),
        amount = amount,
        type = when {
            fromAccountId != null && toAccountId != null -> Transaction.Type.Transfer
            fromAccountId != null -> Transaction.Type.Withdraw
            toAccountId != null -> Transaction.Type.Deposit
            else -> throw IllegalArgumentException("Invalid transaction request")
        },
        createdAt = Clock.System.nowMicros(),
    )