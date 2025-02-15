package com.dn0ne.routing.response

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    val id: String,
    val fromAccountId: String?,
    val toAccountId: String?,
    val amount: Double,
    val createdAt: Instant,
)