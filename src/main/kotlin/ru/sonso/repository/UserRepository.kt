package ru.sonso.repository

import ru.sonso.entity.AdminEntity

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CrudRepository<AdminEntity, UUID> {
    fun findByEmail(email: String): AdminEntity?
}
