package com.dn0ne.repository.table

import com.dn0ne.model.account.Account
import com.dn0ne.model.user.User
import com.dn0ne.repository.database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

class AccountsTableTest {

    private val user = User(
        id = UUID.randomUUID(),
        username = "test",
        password = "test",
        state = User.State.VerificationRequired
    )

    private val account1 = Account(
        id = UUID.randomUUID(),
        holderId = user.id,
        isActive = true
    )

    private val account2 = Account(
        id = UUID.randomUUID(),
        holderId = user.id,
        isActive = true
    )

    private val nonExistentAccount = Account(
        id = UUID.randomUUID(),
        holderId = user.id,
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
            UsersTable.insert(user)
        }

        transaction(database) {
            expectThat(AccountsTable.insert(account1)).isTrue()
            expectThat(AccountsTable.insert(account2)).isTrue()

            expectThat(AccountsTable.insert(account1)).isFalse()
        }
    }

    @Test
    fun `find user accounts`() {
        transaction(database) {
            UsersTable.insert(user)
        }

        transaction(database) {
            expectThat(AccountsTable.selectByHolderId(user.id)).isEmpty()
        }

        transaction(database) {
            AccountsTable.insert(account1)
            AccountsTable.insert(account2)
        }

        transaction(database) {
            expectThat(AccountsTable.selectByHolderId(user.id))
                .isEqualTo(listOf(account1, account2))
        }
    }

    @Test
    fun `find account by id`() {
        transaction(database) {
            UsersTable.insert(user)
        }

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
            UsersTable.insert(user)
        }

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