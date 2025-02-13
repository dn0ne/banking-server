package com.dn0ne.model.user

import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    val password: String,
    val state: State
) {
    enum class State {
        VerificationRequired, Active, Deactivated
    }
}

fun User.isVerified(): Boolean =
    state != User.State.VerificationRequired

fun User.isActive(): Boolean =
    state == User.State.Active
