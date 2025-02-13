package com.dn0ne.model.account

import java.util.UUID

data class Account(
    val id: UUID,
    val holderId: UUID,
    val isActive: Boolean
)
