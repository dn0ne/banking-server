package com.dn0ne.service

import com.dn0ne.model.account.Account
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.UserRepository
import java.util.*

class AccountService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository
) {

    suspend fun findById(id: String): Account? =
        accountRepository.findById(UUID.fromString(id))

    suspend fun findByHolderId(holderId: String): List<Account> =
        accountRepository.findByHolderId(UUID.fromString(holderId))

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
            account
            foundAccount
        }
    }
        }
    }
}