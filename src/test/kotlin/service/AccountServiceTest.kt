package com.dn0ne.service

import com.dn0ne.model.account.Account
import com.dn0ne.model.user.User
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.TransactionRepository
import com.dn0ne.repository.UserRepository
import com.dn0ne.repository.database
import com.dn0ne.repository.table.AccountsTable
import com.dn0ne.repository.table.TransactionsTable
import com.dn0ne.repository.table.UsersTable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

class AccountServiceTest {

    private val accountRepository = AccountRepository()
    private val userRepository = UserRepository()
    private val transactionRepository = TransactionRepository()

    private val accountService = AccountService(
        userRepository = userRepository,
        accountRepository = accountRepository,
        transactionRepository = transactionRepository
    )

    private val user = User(
        id = UUID.randomUUID(),
        username = "test1",
        password = "test1",
        state = User.State.Active
    )
    private val wrongUser = User(
        id = UUID.randomUUID(),
        username = "test2",
        password = "test2",
        state = User.State.Active
    )
    private val nonExistingUser = User(
        id = UUID.randomUUID(),
        username = "not-exist",
        password = "not-exist",
        state = User.State.VerificationRequired
    )

    private val userAccount1 = Account(
        id = UUID.randomUUID(),
        holderId = user.id,
        isActive = true
    )
    private val userAccount2 = Account(
        id = UUID.randomUUID(),
        holderId = user.id,
        isActive = true
    )

    private val nonExistingUserAccount = Account(
        id = UUID.randomUUID(),
        holderId = nonExistingUser.id,
        isActive = true
    )

    @BeforeEach
    fun setUp() {
        transaction(database) {
            SchemaUtils.drop(UsersTable, AccountsTable, TransactionsTable)
            SchemaUtils.create(UsersTable, AccountsTable, TransactionsTable)
        }

        runBlocking {
            userRepository.insert(user)
        }
    }

    @Test
    fun `open account`() = runTest {
        expectThat(accountService.openAccount(userAccount1))
            .isEqualTo(AccountResult.Success)

        expectThat(accountService.openAccount(userAccount2))
            .isEqualTo(AccountResult.Success)

        expectThat(accountService.openAccount(userAccount1))
            .isEqualTo(AccountResult.AlreadyOpened)

        expectThat(accountService.openAccount(nonExistingUserAccount))
            .isEqualTo(AccountResult.HolderNotFound)
    }

    @Test
    fun `find accounts by holder id`() = runTest {
        expectThat(accountService.findByHolderId(user.id.toString()))
            .isEmpty()

        accountService.openAccount(userAccount1)
        accountService.openAccount(userAccount2)

        expectThat(accountService.findByHolderId(user.id.toString()))
            .isEqualTo(listOf(userAccount1, userAccount2))
    }

    @Test
    fun `find account by id`() = runTest {
        expectThat(accountService.findById(userAccount1.id.toString()))
            .isNull()

        accountService.openAccount(userAccount1)
        accountService.openAccount(userAccount2)

        expectThat(accountService.findById(userAccount1.id.toString()))
            .isEqualTo(userAccount1)

        expectThat(accountService.findById(nonExistingUserAccount.id.toString()))
            .isNull()
    }

    @Test
    fun `check account holder`() = runTest {
        accountService.openAccount(userAccount1)

        expectThat(
            accountService.checkHolder(
                userAccount1.id.toString(),
                user.username
            )
        ).isTrue()

        expectThat(
            accountService.checkHolder(
                userAccount2.id.toString(),
                user.username
            )
        ).isFalse()

        expectThat(
            accountService.checkHolder(
                userAccount1.id.toString(),
                wrongUser.username
            )
        ).isFalse()

        expectThat(
            accountService.checkHolder(
                nonExistingUserAccount.id.toString(),
                nonExistingUser.username
            )
        ).isFalse()
    }

    @Test
    fun `close account`() = runTest {
        accountService.openAccount(userAccount1)

        expectThat(accountService.closeAccount(userAccount1.id.toString()))
            .isEqualTo(AccountResult.Success)

        expectThat(accountService.closeAccount(userAccount1.id.toString()))
            .isEqualTo(AccountResult.AlreadyClosed)

        expectThat(accountService.closeAccount(userAccount2.id.toString()))
            .isEqualTo(AccountResult.AccountNotFound)
    }

    @Test
    fun `reopen account`() = runTest {
        accountService.openAccount(userAccount1)
        accountService.closeAccount(userAccount1.id.toString())

        expectThat(accountService.reopenAccount(userAccount1.id.toString()))
            .isEqualTo(AccountResult.Success)

        expectThat(accountService.reopenAccount(userAccount1.id.toString()))
            .isEqualTo(AccountResult.AlreadyOpened)

        expectThat(accountService.reopenAccount(userAccount2.id.toString()))
            .isEqualTo(AccountResult.AccountNotFound)
    }

    @Test
    fun `get account balance`() = runTest {
        accountService.openAccount(userAccount1)

        expectThat(accountService.getBalance(userAccount1))
            .isEqualTo(0.0)

        expectThat(accountService.getBalance(userAccount2))
            .isNull()
    }
}