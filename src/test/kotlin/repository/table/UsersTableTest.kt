package com.dn0ne.repository.table

import com.dn0ne.model.user.User
import com.dn0ne.repository.database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

class UsersTableTest {

    private val user1 = User(
        id = UUID.randomUUID(),
        username = "test1",
        password = "test1",
        state = User.State.VerificationRequired
    )
    private val user2 = User(
        id = UUID.randomUUID(),
        username = "test2",
        password = "test2",
        state = User.State.VerificationRequired
    )
    private val nonExistingUser = User(
        id = UUID.randomUUID(),
        username = "not-exist",
        password = "not-exist",
        state = User.State.VerificationRequired
    )

    @BeforeEach
    fun resetDatabase() {
        transaction(database) {
            SchemaUtils.drop(UsersTable)
            SchemaUtils.create(UsersTable)
        }
    }

    @Test
    fun `add user`() {
        transaction(database) {
            expectThat(UsersTable.all()).isEmpty()
        }

        transaction(database) {
            expectThat(UsersTable.insert(user1)).isTrue()
            expectThat(UsersTable.insert(user2)).isTrue()

            expectThat(UsersTable.insert(user2)).isFalse()

            expectThat(UsersTable.all())
                .isEqualTo(listOf(user1, user2))
        }
    }

    @Test
    fun `find user by id`() {
        transaction(database) {
            UsersTable.insert(user1)
            UsersTable.insert(user2)
        }

        transaction(database) {
            expectThat(UsersTable.selectById(nonExistingUser.id)).isNull()
            expectThat(UsersTable.selectById(user1.id)).isEqualTo(user1)
        }
    }

    @Test
    fun `find user by username`() {
        transaction(database) {
            UsersTable.insert(user1)
            UsersTable.insert(user2)
        }

        transaction(database) {
            expectThat(UsersTable.selectByUsername(nonExistingUser.username)).isNull()
            expectThat(UsersTable.selectByUsername(user1.username)).isEqualTo(user1)
        }
    }

    @Test
    fun `update user`() {
        transaction(database) {
            UsersTable.insert(user1)
        }

        transaction(database) {
            val revisedUser = user1.copy(
                username = "updated1",
                password = "updated1",
                state = User.State.Active
            )

            expectThat(
                UsersTable.update(revisedUser)
            ).isTrue()

            expectThat(UsersTable.selectById(revisedUser.id))
                .isEqualTo(revisedUser)

            expectThat(
                UsersTable.update(nonExistingUser)
            ).isFalse()
        }
    }
}
