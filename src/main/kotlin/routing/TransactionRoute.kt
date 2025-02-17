package com.dn0ne.routing

import com.dn0ne.model.Transaction
import com.dn0ne.repository.table.nowMicros
import com.dn0ne.routing.request.TransactionRequest
import com.dn0ne.routing.request.verify
import com.dn0ne.routing.response.TransactionResponse
import com.dn0ne.service.AccountService
import com.dn0ne.service.TransactionResult
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
                ?: run {
                    call.application.environment.log.info("TransactionRequest is not valid")
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
        } catch (e: ContentTransformationException) {
            call.application.environment.log.info("Failed to parse transaction request")
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val username = extractPrincipalUsername(call)
            ?: return@post call.respond(HttpStatusCode.Forbidden)

        transactionRequest.fromAccountId?.let {
            val isSenderAccountHolder = accountService.checkHolder(it, username)
            if (!isSenderAccountHolder) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }
        }

        val transaction = try {
            transactionRequest.toModel()
        } catch (e: IllegalArgumentException) {
            call.application.environment.log.info("Failed to convert transaction request: $transactionRequest")
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        when (val result = transactionService.processTransaction(transaction)) {
            TransactionResult.Accepted -> return@post call.respond(HttpStatusCode.OK)
            TransactionResult.BadTransaction -> return@post call.respond(HttpStatusCode.BadRequest)
            else -> return@post call.respond(
                HttpStatusCode.Conflict,
                mapOf(
                    "error" to result
                )
            )
        }
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
        fromAccountId = fromAccountId?.let { UUID.fromString(it) },
        toAccountId = toAccountId?.let { UUID.fromString(it) },
        amount = amount,
        type = when {
            fromAccountId != null && toAccountId != null -> Transaction.Type.Transfer
            fromAccountId != null -> Transaction.Type.Withdraw
            toAccountId != null -> Transaction.Type.Deposit
            else -> throw IllegalArgumentException("Invalid transaction request")
        },
        createdAt = Clock.System.nowMicros(),
    )