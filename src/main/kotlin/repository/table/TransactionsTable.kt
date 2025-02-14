package com.dn0ne.repository.table

import com.dn0ne.model.Transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.util.*

object TransactionsTable: IdTable<UUID>() {
    override val id = uuid("id").entityId().uniqueIndex()
    val fromAccountId = uuid("from_account_id").nullable()
    val toAccountId = uuid("to_account_id").nullable()
    val amount = double("amount")
    val type = enumerationByName("type", 20, Transaction.Type::class)
    val createdAt = timestamp("created_at")
}

fun TransactionsTable.insert(transaction: Transaction): Boolean {
    val insertResult = insertIgnore {
        it[id] = transaction.id
        it[fromAccountId] = transaction.fromAccountId
        it[toAccountId] = transaction.toAccountId
        it[amount] = transaction.amount
        it[type] = transaction.type
        it[createdAt] = transaction.createdAt
    }

    return insertResult.insertedCount == 1
}

fun TransactionsTable.selectByAccountId(id: UUID): List<Transaction> =
    selectAll()
        .where { fromAccountId eq id or (toAccountId eq id) }
        .map {
            it.toTransaction()
        }

fun ResultRow.toTransaction() = Transaction(
    id = this[TransactionsTable.id].value,
    fromAccountId = this[TransactionsTable.fromAccountId],
    toAccountId = this[TransactionsTable.toAccountId],
    amount = this[TransactionsTable.amount],
    type = this[TransactionsTable.type],
    createdAt = this[TransactionsTable.createdAt],
)

fun Clock.System.nowMicros(): Instant {
    return now().run {
        Instant.fromEpochSeconds(
            epochSeconds,
            nanosecondsOfSecond / 1000 * 1000
        )
    }
}