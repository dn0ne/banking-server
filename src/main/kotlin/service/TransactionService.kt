package com.dn0ne.service

import com.dn0ne.model.Transaction
import com.dn0ne.model.isDeposit
import com.dn0ne.model.isTransfer
import com.dn0ne.model.isWithdraw
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.TransactionRepository
import java.util.UUID

class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
) {

    suspend fun findByAccountId(accountId: String): List<Transaction> =
        transactionRepository.findByAccountId(UUID.fromString(accountId))

    suspend fun processTransaction(transaction: Transaction): Boolean {
        if (transaction.amount <= 0) return false

        val senderAccount = transaction.fromAccountId?.let {
            accountRepository.findById(it)
        }

        if (senderAccount == null && !transaction.isDeposit() || transaction.isTransfer())
            return false

        val recipientAccount = transaction.toAccountId?.let {
            accountRepository.findById(it)
        }

        if (recipientAccount == null && !transaction.isWithdraw() || transaction.isTransfer())
            return false

        return transactionRepository.insert(transaction)
    }
}