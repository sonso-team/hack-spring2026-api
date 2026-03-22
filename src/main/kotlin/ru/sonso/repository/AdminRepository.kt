package ru.sonso.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sonso.entity.AdminEntity
import ru.sonso.enumerable.AdminRole
import java.util.UUID

@Repository
interface AdminRepository : JpaRepository<AdminEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): AdminEntity?
    fun findAllByRole(role: AdminRole): List<AdminEntity>
}
