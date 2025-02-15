package com.dn0ne.service

import com.dn0ne.model.Transaction
import com.dn0ne.model.isDeposit
import com.dn0ne.model.isTransfer
import com.dn0ne.model.isWithdraw
import com.dn0ne.repository.TransactionRepository
import org.slf4j.LoggerFactory
import java.util.*

class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    suspend fun findByAccountId(accountId: String): List<Transaction> =
        transactionRepository.findByAccountId(UUID.fromString(accountId))

    suspend fun processTransaction(transaction: Transaction): Boolean {
        if (transaction.amount <= 0) {
            logger.info("Transaction rejected: amount (${transaction.amount}) must be positive")
            return false
        }

        val senderAccount = transaction.fromAccountId?.let {
            accountService.findById(it.toString())
        }

        if (senderAccount == null && !transaction.isDeposit() || transaction.isTransfer()) {
            logger.info("Transaction rejected: sender must be specified for transaction of type ${transaction.type}")
            return false
        }

        senderAccount?.let {
            val balance = accountService.getBalance(it) ?: run {
                logger.info("Transaction rejected: account ${senderAccount.id} not found")
                return false
            }

            if (balance < transaction.amount) {
                logger.info("Transaction rejected: insufficient funds in account ${senderAccount.id}")
                return false
            }
        }

        val recipientAccount = transaction.toAccountId?.let {
            accountService.findById(it.toString())
        }

        if (recipientAccount == null && !transaction.isWithdraw() || transaction.isTransfer()) {
            logger.info("Transaction rejected: recipient must be specified for transaction of type ${transaction.type}")
            return false
        }

        return transactionRepository.insert(transaction).also {
            if (it) {
                logger.info("Transaction ${transaction.id} completed")
            }
        }
    }
}