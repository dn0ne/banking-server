package com.dn0ne.service

import com.dn0ne.model.Transaction
import com.dn0ne.model.isDeposit
import com.dn0ne.model.isTransfer
import com.dn0ne.model.isWithdraw
import com.dn0ne.repository.TransactionRepository
import java.util.*

class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
) {

    suspend fun findByAccountId(accountId: String): List<Transaction> =
        transactionRepository.findByAccountId(UUID.fromString(accountId))

    suspend fun processTransaction(transaction: Transaction): Boolean {
        if (transaction.amount <= 0) return false

        val senderAccount = transaction.fromAccountId?.let {
            accountService.findById(it.toString())
        }

        if (senderAccount == null && !transaction.isDeposit() || transaction.isTransfer())
            return false

        senderAccount?.let {
            val balance = accountService.getBalance(it) ?: return false
            if (balance < transaction.amount) return false
        }

        val recipientAccount = transaction.toAccountId?.let {
            accountService.findById(it.toString())
        }

        if (recipientAccount == null && !transaction.isWithdraw() || transaction.isTransfer())
            return false

        return transactionRepository.insert(transaction)
    }
}