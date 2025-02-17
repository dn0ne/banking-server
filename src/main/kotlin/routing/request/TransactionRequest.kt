package com.dn0ne.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val amount: Double
)

fun TransactionRequest.verify(): Boolean =
    fromAccountId != null || toAccountId != null && amount > 0
