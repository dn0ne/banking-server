package com.dn0ne.repository.table

import com.dn0ne.model.account.Account
import com.dn0ne.repository.database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

class AccountsTableTest {

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
            SchemaUtils.drop(UsersTable)
            SchemaUtils.create(UsersTable)
            SchemaUtils.drop(AccountsTable)
            SchemaUtils.create(AccountsTable)
        }
    }

    @Test
    fun `open account`() {
        transaction(database) {
            expectThat(AccountsTable.insert(account1)).isTrue()
            expectThat(AccountsTable.insert(account2)).isTrue()

            expectThat(AccountsTable.insert(account1)).isFalse()
        }
    }

    @Test
    fun `find user accounts`() {
        transaction(database) {
            expectThat(AccountsTable.selectByHolderId(holderId)).isEmpty()
        }

        transaction(database) {
            AccountsTable.insert(account1)
            AccountsTable.insert(account2)
        }

        transaction(database) {
            expectThat(AccountsTable.selectByHolderId(holderId))
                .isEqualTo(listOf(account1, account2))
        }
    }

    @Test
    fun `find account by id`() {
        transaction(database) {
            AccountsTable.insert(account1)
            AccountsTable.insert(account2)
        }

        transaction(database) {
            expectThat(AccountsTable.selectById(nonExistentAccount.id)).isNull()
            expectThat(AccountsTable.selectById(account1.id)).isEqualTo(account1)
        }
    }

    @Test
    fun `update account`() {
        transaction(database) {
            AccountsTable.insert(account1)
        }

        val revisedAccount = account1.copy(
            isActive = false
        )

        transaction(database) {
            expectThat(AccountsTable.update(revisedAccount)).isTrue()

            expectThat(AccountsTable.selectById(revisedAccount.id))
                .isEqualTo(revisedAccount)

            expectThat(AccountsTable.selectByHolderId(revisedAccount.holderId))
                .contains(revisedAccount)

            expectThat(AccountsTable.selectByHolderId(revisedAccount.holderId))
                .doesNotContain(account1)

        }
    }
}