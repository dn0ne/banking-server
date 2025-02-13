package com.dn0ne.repository.table

import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

object AccountsTable: IdTable<UUID>() {
    override val id = uuid("id").entityId().uniqueIndex()
    val holderId = uuid("holder_id")
    val isActive = bool("is_active")
}