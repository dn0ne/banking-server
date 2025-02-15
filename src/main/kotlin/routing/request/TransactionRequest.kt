package com.dn0ne.routing.request

data class TransactionRequest(
    val fromAccountId: String?,
    val toAccountId: String?,
    val amount: Double
)

fun TransactionRequest.verify(): Boolean =
    fromAccountId != null || toAccountId != null && amount > 0
