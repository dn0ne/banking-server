package com.dn0ne.repository.table

import com.dn0ne.model.Transaction
import com.dn0ne.repository.database
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

class TransactionsTableTest {

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
    fun `add transaction`() {
        transaction(database) {
            expectThat(TransactionsTable.insert(transferTransaction)).isTrue()
            expectThat(TransactionsTable.insert(transferTransaction)).isFalse()
        }

        transaction(database) {
            expectThat(TransactionsTable.insert(depositTransaction)).isTrue()
            expectThat(TransactionsTable.insert(depositTransaction)).isFalse()
        }

        transaction(database) {
            expectThat(TransactionsTable.insert(withdrawTransaction)).isTrue()
            expectThat(TransactionsTable.insert(withdrawTransaction)).isFalse()
        }
    }

    @Test
    fun `find transactions by accountId`() {
        transaction(database) {
            expectThat(TransactionsTable.selectByAccountId(account1Id)).isEmpty()
        }

        transaction(database) {
            TransactionsTable.insert(transferTransaction)
            TransactionsTable.insert(depositTransaction)
            TransactionsTable.insert(withdrawTransaction)
        }

        transaction(database) {
            expectThat(TransactionsTable.selectByAccountId(account1Id))
                .isEqualTo(listOf(transferTransaction, depositTransaction, withdrawTransaction))
        }
    }
}