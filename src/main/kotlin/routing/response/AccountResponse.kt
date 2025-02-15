package com.dn0ne.routing.response

import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    val id: String,
    val isActive: Boolean,
    val balance: Double
)
