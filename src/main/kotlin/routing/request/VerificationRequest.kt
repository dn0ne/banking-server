package com.dn0ne.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class VerificationRequest(
    val code: String,
    val username: String
)
