package com.dn0ne.repository

import com.dn0ne.model.user.User
import com.dn0ne.repository.table.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class UserRepository {

    suspend fun findById(id: UUID): User? = dbQuery {
        UsersTable.selectById(id)
    }

    suspend fun findByUsername(username: String): User? = dbQuery {
        UsersTable.selectByUsername(username)
    }

    suspend fun insert(user: User): Boolean = dbQuery {
        UsersTable.insert(user)
    }

    suspend fun update(user: User): Boolean = dbQuery {
        UsersTable.update(user)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
}