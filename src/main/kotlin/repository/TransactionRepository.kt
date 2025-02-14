package com.dn0ne.repository

import com.dn0ne.model.Transaction
import com.dn0ne.repository.table.TransactionsTable
import com.dn0ne.repository.table.insert
import com.dn0ne.repository.table.selectByAccountId
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TransactionRepository {

    suspend fun findByAccountId(accountId: UUID): List<Transaction> = dbQuery {
        TransactionsTable.selectByAccountId(accountId)
    }

    suspend fun insert(transaction: Transaction): Boolean = dbQuery {
        TransactionsTable.insert(transaction)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
}