package com.dn0ne.repository.table

import com.dn0ne.model.user.User
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

object UsersTable : IdTable<UUID>() {
    override val id = uuid("id").entityId().uniqueIndex()
    val username = varchar("username", 128).uniqueIndex()
    val password = varchar("password", 128)
    val state = enumerationByName("state", 20, User.State::class)
}

fun UsersTable.insert(user: User): Boolean {
    val insertResult = insertIgnore {
        it[id] = user.id
        it[username] = user.username
        it[password] = user.password
        it[state] = user.state
    }

    return insertResult.insertedCount == 1
}

fun UsersTable.all() = selectAll().map { it.toUser() }

fun UsersTable.selectById(id: UUID): User? {
    val foundUsers = selectAll()
        .where { UsersTable.id eq id }
        .map { it.toUser() }

    return when {
        foundUsers.isEmpty() -> null
        foundUsers.size == 1 -> foundUsers.first()
        else -> throw IllegalStateException("Multiple users with the same id: $id")
    }
}

fun UsersTable.selectByUsername(username: String): User? {
    val foundUsers = selectAll()
        .where { UsersTable.username eq username }
        .map { it.toUser() }

    return when {
        foundUsers.isEmpty() -> null
        foundUsers.size == 1 -> foundUsers.first()
        else -> throw IllegalStateException("Multiple users with the same username: $username")
    }
}

fun UsersTable.update(user: User): Boolean {
    val updatedCount = update({ id eq user.id }) {
        it[username] = user.username
        it[password] = user.password
        it[state] = user.state
    }

    return when (updatedCount) {
        0 -> false
        1 -> true
        else -> throw IllegalStateException("Updated multiple users with id: $id")
    }
}

fun ResultRow.toUser() = User(
    id = this[UsersTable.id].value,
    username = this[UsersTable.username],
    password = this[UsersTable.password],
    state = this[UsersTable.state],
)