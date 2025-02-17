package com.dn0ne.service

import com.dn0ne.model.Transaction
import com.dn0ne.model.isDeposit
import com.dn0ne.model.isWithdraw
import com.dn0ne.repository.TransactionRepository
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.util.*

class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    suspend fun findByAccountId(accountId: String): List<Transaction> =
        transactionRepository.findByAccountId(UUID.fromString(accountId))

    suspend fun processTransaction(transaction: Transaction): TransactionResult {
        if (transaction.amount <= 0) {
            logger.info("Transaction rejected: amount (${transaction.amount}) must be positive")
            return TransactionResult.BadTransaction
        }

        if (transaction.fromAccountId == transaction.toAccountId) {
            logger.info("Transaction rejected: sender and receiver are the same account ${transaction.fromAccountId}")
            return TransactionResult.BadTransaction
        }

        val senderAccount = transaction.fromAccountId?.let {
            accountService.findById(it.toString())
        }

        if (senderAccount == null && !transaction.isDeposit()) {
            logger.info("Transaction rejected: sender must be specified for transaction of type ${transaction.type}")
            return TransactionResult.BadTransaction
        }

        senderAccount?.let {
            if (!it.isActive) return TransactionResult.AccountIsClosed

            val balance = accountService.getBalance(it) ?: run {
                logger.info("Transaction rejected: account ${senderAccount.id} not found")
                return TransactionResult.AccountNotFound
            }

            if (balance < transaction.amount) {
                logger.info("Transaction rejected: insufficient funds in account ${senderAccount.id}")
                return TransactionResult.InsufficientFunds
            }
        }

        val recipientAccount = transaction.toAccountId?.let {
            accountService.findById(it.toString())
        }

        if (recipientAccount == null && !transaction.isWithdraw()) {
            logger.info("Transaction rejected: recipient must be specified for transaction of type ${transaction.type}")
            return TransactionResult.BadTransaction
        }

        recipientAccount?.let {
            if (!it.isActive) return TransactionResult.AccountIsClosed
        }

        val isAccepted = transactionRepository.insert(transaction)

        return if (isAccepted) {
            logger.info("Transaction ${transaction.id} completed")
            TransactionResult.Accepted
        } else {
            logger.error("Failed to process transaction ${transaction.id}")
            TransactionResult.InternalError
        }
    }
}

@Serializable
enum class TransactionResult {
    Accepted,
    InsufficientFunds,
    BadTransaction,
    AccountIsClosed,
    AccountNotFound,
    InternalError
}