package com.dn0ne.model

import kotlinx.datetime.Instant
import java.util.*

data class Transaction(
    val id: UUID,
    val fromAccountId: UUID?,
    val toAccountId: UUID?,
    val amount: Double,
    val type: Type,
    val createdAt: Instant
) {
    init {
        require(fromAccountId != null || toAccountId != null) {
            "Transaction must have at least one accountId (either fromAccountId or toAccountId)"
        }
    }

    enum class Type {
        Transfer, Deposit, Withdraw
    }
}

fun Transaction.isTransfer() = type == Transaction.Type.Transfer
fun Transaction.isWithdraw() = type == Transaction.Type.Withdraw
fun Transaction.isDeposit() = type == Transaction.Type.Deposit