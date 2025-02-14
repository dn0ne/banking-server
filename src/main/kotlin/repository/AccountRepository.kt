package com.dn0ne.repository

import com.dn0ne.model.account.Account
import com.dn0ne.repository.table.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AccountRepository {

    suspend fun insert(account: Account): Boolean = dbQuery {
        AccountsTable.insert(account)
    }

    suspend fun findById(id: UUID): Account? = dbQuery {
        AccountsTable.selectById(id)
    }

    suspend fun findByHolderId(id: UUID): List<Account> = dbQuery {
        AccountsTable.selectByHolderId(id)
    }

    suspend fun update(account: Account): Boolean = dbQuery {
        AccountsTable.update(account)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
}