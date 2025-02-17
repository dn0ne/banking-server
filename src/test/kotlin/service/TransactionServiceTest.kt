package com.dn0ne.service

import com.dn0ne.model.Transaction
import com.dn0ne.model.account.Account
import com.dn0ne.model.user.User
import com.dn0ne.repository.AccountRepository
import com.dn0ne.repository.TransactionRepository
import com.dn0ne.repository.UserRepository
import com.dn0ne.repository.database
import com.dn0ne.repository.table.AccountsTable
import com.dn0ne.repository.table.TransactionsTable
import com.dn0ne.repository.table.UsersTable
import com.dn0ne.repository.table.nowMicros
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*

class TransactionServiceTest {

    private val transactionRepository = TransactionRepository()
    private val accountRepository = AccountRepository()
    private val userRepository = UserRepository()
    private val accountService = AccountService(
        accountRepository = accountRepository,
        userRepository = userRepository,
        transactionRepository = transactionRepository
    )

    private val transactionService = TransactionService(
        transactionRepository = transactionRepository,
        accountService = accountService
    )

    private val user1 = User(
        id = UUID.randomUUID(),
        username = "test1",
        password = "test1",
        state = User.State.Active
    )
    private val user2 = User(
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

    private val user1Account1 = Account(
        id = UUID.randomUUID(),
        holderId = user1.id,
        isActive = true
    )
    private val user1Account2 = Account(
        id = UUID.randomUUID(),
        holderId = user1.id,
        isActive = true
    )

    private val user2Account1 = Account(
        id = UUID.randomUUID(),
        holderId = user2.id,
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
            userRepository.insert(user1)
            userRepository.insert(user2)

            accountRepository.insert(user1Account1)
            accountRepository.insert(user1Account2)
            accountRepository.insert(user2Account1)

            accountService.closeAccount(user1Account2.id.toString())
        }
    }

    @Test
    fun `process transaction`() = runTest {
        expectThat(
            transactionService.processTransaction(
                createDepositTransaction(user1Account1, 500.0)
            )
        ).isEqualTo(TransactionResult.Accepted)

        expectThat(
            transactionService.processTransaction(
                createWithdrawTransaction(user1Account1, 100.0)
            )
        ).isEqualTo(TransactionResult.Accepted)

        expectThat(
            transactionService.processTransaction(
                createTransferTransaction(
                    user1Account1,
                    user2Account1,
                    100.0
                )
            )
        ).isEqualTo(TransactionResult.Accepted)

        expectThat(
            transactionService.processTransaction(
                createDepositTransaction(
                    nonExistingUserAccount,
                    150.0
                )
            )
        ).isEqualTo(TransactionResult.BadTransaction)

        expectThat(
            transactionService.processTransaction(
                createWithdrawTransaction(
                    user1Account1,
                    10000.0
                )
            )
        ).isEqualTo(TransactionResult.InsufficientFunds)

        expectThat(
            transactionService.processTransaction(
                createWithdrawTransaction(
                    user1Account2,
                    100.0
                )
            )
        ).isEqualTo(TransactionResult.AccountIsClosed)

        expectThat(
            transactionService.processTransaction(
                createTransactionWithInvalidAmount()
            )
        ).isEqualTo(TransactionResult.BadTransaction)

        expectThat(
            transactionService.processTransaction(
                createTransactionWithInvalidType()
            )
        ).isEqualTo(TransactionResult.BadTransaction)
    }

    @Test
    fun `find transactions by account id`() = runTest {
        val transactions = listOf(
            createDepositTransaction(user1Account1, 500.0),
            createWithdrawTransaction(user1Account1, 100.0),
            createTransferTransaction(user1Account1, user2Account1, 300.0),
            createWithdrawTransaction(user1Account1, 100.0)
        )

        transactions.forEach {
            transactionService.processTransaction(it)
        }

        expectThat(transactionService.findByAccountId(user1Account1.id.toString()))
            .isEqualTo(transactions)

        expectThat(transactionService.findByAccountId(user2Account1.id.toString()))
            .isEqualTo(listOf(transactions[2]))
    }

    private fun createDepositTransaction(
        toAccount: Account,
        amount: Double
    ) = Transaction(
        id = UUID.randomUUID(),
        fromAccountId = null,
        toAccountId = toAccount.id,
        amount = amount,
        type = Transaction.Type.Deposit,
        createdAt = Clock.System.nowMicros()
    )

    private fun createWithdrawTransaction(
        fromAccount: Account,
        amount: Double
    ) = Transaction(
        id = UUID.randomUUID(),
        fromAccountId = fromAccount.id,
        toAccountId = null,
        amount = amount,
        type = Transaction.Type.Withdraw,
        createdAt = Clock.System.nowMicros()
    )

    private fun createTransferTransaction(
        fromAccount: Account,
        toAccount: Account,
        amount: Double
    ) = Transaction(
        id = UUID.randomUUID(),
        fromAccountId = fromAccount.id,
        toAccountId = toAccount.id,
        amount = amount,
        type = Transaction.Type.Transfer,
        createdAt = Clock.System.nowMicros()
    )

    private fun createTransactionWithInvalidAmount() =
        Transaction(
            id = UUID.randomUUID(),
            fromAccountId = user1Account1.id,
            toAccountId = user1Account2.id,
            amount = -100.0,
            type = Transaction.Type.Transfer,
            createdAt = Clock.System.nowMicros()
        )

    private fun createTransactionWithInvalidType() =
        Transaction(
            id = UUID.randomUUID(),
            fromAccountId = null,
            toAccountId = user1Account2.id,
            amount = 100.0,
            type = Transaction.Type.Transfer,
            createdAt = Clock.System.nowMicros()
        )
}