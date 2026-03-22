package ru.sonso.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sonso.entity.AdminEntity
import java.util.UUID

@Repository
interface AdminRepository : JpaRepository<AdminEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): AdminEntity?
}
