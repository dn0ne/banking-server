package com.dn0ne.service

import com.dn0ne.model.account.Account
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.TransactionRepository
import com.dn0ne.repository.UserRepository
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.util.*

class AccountService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    private val logger = LoggerFactory.getLogger(AccountService::class.java)

    suspend fun findById(id: String): Account? =
        accountRepository.findById(UUID.fromString(id))

    suspend fun findByHolderId(holderId: String): List<Account> =
        accountRepository.findByHolderId(UUID.fromString(holderId))

    suspend fun checkHolder(accountId: String, holderUsername: String): Boolean {
        val account = findById(accountId) ?: return false
        val holder = userRepository.findByUsername(holderUsername) ?: return false

        return account.holderId == holder.id
    }

    suspend fun openAccount(account: Account): AccountResult {
        val foundUser = userRepository.findById(account.holderId)

        return foundUser?.let {
            val foundAccount = accountRepository.findById(account.id)

            if (foundAccount == null) {
                if (accountRepository.insert(account)) {
                    logger.info("Created account ${account.id} for ${account.holderId}")
                    AccountResult.Success
                } else {
                    logger.error("Failed to create account for ${account.holderId}")
                    AccountResult.InternalError
                }
            } else AccountResult.AlreadyOpened
        } ?: run {
            logger.info("Unable to open account for ${account.holderId}: user not found")
            AccountResult.HolderNotFound
        }
    }

    suspend fun closeAccount(id: String): AccountResult {
        val foundAccount = accountRepository.findById(UUID.fromString(id))

        return foundAccount?.let {
            if (!it.isActive) {
                logger.info("Account $id is already closed")
                AccountResult.AlreadyClosed
            } else {
                val revisedAccount = foundAccount.copy(
                    isActive = false,
                )
                val isUpdated = accountRepository.update(revisedAccount)

                if (isUpdated) {
                    logger.info("Account $id is closed")
                    AccountResult.Success
                } else {
                    logger.info("Failed to close account $id")
                    AccountResult.InternalError
                }
            }
        } ?: AccountResult.AccountNotFound
    }

    suspend fun reopenAccount(id: String): AccountResult {
        val foundAccount = accountRepository.findById(UUID.fromString(id))

        return foundAccount?.let {
            if (it.isActive) {
                logger.info("Account $id is already opened")
                AccountResult.AlreadyOpened
            } else {
                val revisedAccount = foundAccount.copy(
                    isActive = true,
                )
                val isUpdated = accountRepository.update(revisedAccount)

                if (isUpdated) {
                    logger.info("Account $id is reopened")
                    AccountResult.Success
                } else {
                    logger.info("Failed to reopen account $id")
                    AccountResult.InternalError
                }
            }
        } ?: AccountResult.AccountNotFound
    }

    suspend fun getBalance(account: Account): Double? {
        val foundAccount = accountRepository
            .findById(account.id)

        return foundAccount?.let { acc ->
            val transactions = transactionRepository.findByAccountId(acc.id)

            transactions.fold(0.0) { sum, transaction ->
                sum + when {
                    transaction.fromAccountId == acc.id -> -transaction.amount
                    transaction.toAccountId == acc.id -> transaction.amount
                    else -> throw IllegalStateException(
                        "Transaction with id ${transaction.id} " +
                                "does not belong to the account with id ${acc.id}."
                    )
                }
            }
        }
    }
}

@Serializable
enum class AccountResult {
    Success,
    HolderNotFound,
    AccountNotFound,
    AlreadyOpened,
    AlreadyClosed,
    InternalError
}