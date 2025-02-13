package com.dn0ne.repository.table

import com.dn0ne.model.account.Account
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

object AccountsTable: IdTable<UUID>() {
    override val id = uuid("id").entityId().uniqueIndex()
    val holderId = uuid("holder_id")
    val isActive = bool("is_active")
}

fun AccountsTable.insert(account: Account): Boolean {
    val insertResult = insertIgnore {
        it[id] = account.id
        it[holderId] = account.holderId
        it[isActive] = account.isActive
    }

    return insertResult.insertedCount == 1
}

fun AccountsTable.selectByHolderId(id: UUID): List<Account> =
    selectAll()
        .where { holderId eq id }
        .map {
            it.toAccount()
        }

fun AccountsTable.selectById(id: UUID): Account? {
    val foundAccounts = selectAll()
        .where { AccountsTable.id eq id }
        .map {
            it.toAccount()
        }

    return when {
        foundAccounts.isEmpty() -> null
        foundAccounts.size == 1 -> foundAccounts.first()
        else -> throw IllegalStateException("Multiple accounts with the same id: $id")
    }
}

fun AccountsTable.update(account: Account): Boolean {
    val updatedCount = update({ id eq account.id }) {
        it[isActive] = account.isActive
    }

    return when(updatedCount) {
        0 -> false
        1 -> true
        else -> throw IllegalStateException("Updated multiple accounts with id: $id")
    }
}

fun ResultRow.toAccount(): Account = Account(
    id = this[AccountsTable.id].value,
    holderId = this[AccountsTable.holderId],
    isActive = this[AccountsTable.isActive],
)