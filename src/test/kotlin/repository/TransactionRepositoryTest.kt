package com.dn0ne.repository

import com.dn0ne.model.Transaction
import com.dn0ne.repository.table.TransactionsTable
import com.dn0ne.repository.table.nowMicros
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.*

class TransactionRepositoryTest {

    private val transactionRepository = TransactionRepository()

    private val account1Id = UUID.randomUUID()
    private val account2Id = UUID.randomUUID()

    private val transferTransaction = Transaction(
        id = UUID.randomUUID(),
        fromAccountId = account1Id,
        toAccountId = account2Id,
        amount = 100.0,
        type = Transaction.Type.Transfer,
        createdAt = Clock.System.nowMicros(),
    )

    private val depositTransaction = Transaction(
        id = UUID.randomUUID(),
        fromAccountId = null,
        toAccountId = account1Id,
        amount = 100.0,
        type = Transaction.Type.Deposit,
        createdAt = Clock.System.nowMicros()
    )

    private val withdrawTransaction = Transaction(
        id = UUID.randomUUID(),
        fromAccountId = account1Id,
        toAccountId = null,
        amount = 100.0,
        type = Transaction.Type.Withdraw,
        createdAt = Clock.System.nowMicros(),
    )

    @BeforeEach
    fun resetDatabase() {
        transaction(database) {
            SchemaUtils.drop(TransactionsTable)
            SchemaUtils.create(TransactionsTable)
        }
    }

    @Test
    fun `add transaction to repository`() = runTest {
        expectThat(transactionRepository.insert(transferTransaction)).isTrue()
        expectThat(transactionRepository.insert(transferTransaction)).isFalse()

        expectThat(transactionRepository.insert(depositTransaction)).isTrue()
        expectThat(transactionRepository.insert(depositTransaction)).isFalse()

        expectThat(transactionRepository.insert(withdrawTransaction)).isTrue()
        expectThat(transactionRepository.insert(withdrawTransaction)).isFalse()
    }

    @Test
    fun `find transactions in repository by accountId`() = runTest {
        expectThat(transactionRepository.findByAccountId(account1Id)).isEmpty()

        transactionRepository.insert(transferTransaction)
        transactionRepository.insert(depositTransaction)
        transactionRepository.insert(withdrawTransaction)

        expectThat(transactionRepository.findByAccountId(account1Id))
            .isEqualTo(listOf(transferTransaction, depositTransaction, withdrawTransaction))
    }
}