package com.dn0ne.service

import com.dn0ne.model.account.Account
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.TransactionRepository
import com.dn0ne.repository.UserRepository
import java.util.*

class AccountService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend fun findById(id: String): Account? =
        accountRepository.findById(UUID.fromString(id))

    suspend fun findByHolderId(holderId: String): List<Account> =
        accountRepository.findByHolderId(UUID.fromString(holderId))

    suspend fun checkHolder(accountId: String, holderUsername: String): Boolean {
        val account = findById(accountId) ?: return false
        val holder = userRepository.findByUsername(holderUsername) ?: return false

        return account.holderId == holder.id
    }

    suspend fun openAccount(account: Account): Account? {
        val foundUser = userRepository.findById(account.holderId)

        return foundUser?.let {
            val foundAccount = accountRepository.findById(account.id)

            if (foundAccount == null) {
                accountRepository.insert(account)
                account
            } else null
        }
    }

    suspend fun closeAccount(id: String): Account? {
        val foundAccount = accountRepository.findById(UUID.fromString(id))

        return foundAccount?.let {
            accountRepository.update(
                foundAccount.copy(
                    isActive = false
                )
            )
            foundAccount
        }
    }

    suspend fun getBalance(account: Account): Double? {
        val foundAccount = accountRepository
            .findById(account.id)
            ?.takeIf { it.isActive }

        return foundAccount?.let { acc ->
            val transactions = transactionRepository.findByAccountId(acc.id)

            transactions.fold(0.0) { sum, transaction ->
                sum + when {
                    transaction.fromAccountId == acc.id -> sum - transaction.amount
                    transaction.toAccountId == acc.id -> sum + transaction.amount
                    else -> throw IllegalStateException(
                        "Transaction with id ${transaction.id} " +
                                "does not belong to the account with id ${acc.id}."
                    )
                }
            }
        }
    }
}