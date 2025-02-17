package com.dn0ne.repository

import com.dn0ne.model.account.Account
import com.dn0ne.repository.table.AccountsTable
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

class AccountRepositoryTest {

    private val accountRepository = AccountRepository()

    private val holderId = UUID.randomUUID()

    private val account1 = Account(
        id = UUID.randomUUID(),
        holderId = holderId,
        isActive = true
    )

    private val account2 = Account(
        id = UUID.randomUUID(),
        holderId = holderId,
        isActive = true
    )

    private val nonExistentAccount = Account(
        id = UUID.randomUUID(),
        holderId = holderId,
        isActive = false
    )

    @BeforeEach
    fun resetDatabase() {
        transaction(database) {
            SchemaUtils.drop(AccountsTable)
            SchemaUtils.create(AccountsTable)
        }
    }

    @Test
    fun `add account to repository`() = runTest {
        expectThat(accountRepository.insert(account1)).isTrue()
        expectThat(accountRepository.insert(account2)).isTrue()

        expectThat(accountRepository.insert(account1)).isFalse()
    }

    @Test
    fun `find user accounts in repository`() = runTest {
        expectThat(accountRepository.findByHolderId(holderId)).isEmpty()

        accountRepository.insert(account1)
        accountRepository.insert(account2)

        expectThat(accountRepository.findByHolderId(holderId))
            .isEqualTo(listOf(account1, account2))
    }

    @Test
    fun `find account in repository by id`() = runTest {
        accountRepository.insert(account1)
        accountRepository.insert(account2)

        expectThat(accountRepository.findById(nonExistentAccount.id)).isNull()
        expectThat(accountRepository.findById(account1.id)).isEqualTo(account1)
    }

    @Test
    fun `update account in repository`() = runTest {
        accountRepository.insert(account1)

        val revisedAccount = account1.copy(
            isActive = false
        )

        expectThat(accountRepository.update(revisedAccount)).isTrue()

        expectThat(accountRepository.findById(revisedAccount.id))
            .isEqualTo(revisedAccount)

        expectThat(accountRepository.findByHolderId(revisedAccount.holderId))
            .contains(revisedAccount)

        expectThat(accountRepository.findByHolderId(revisedAccount.holderId))
            .doesNotContain(account1)
    }
}