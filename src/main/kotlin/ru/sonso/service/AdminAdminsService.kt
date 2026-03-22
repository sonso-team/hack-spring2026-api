package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sonso.dto.AdminFull
import ru.sonso.dto.SuccessResponse
import ru.sonso.dto.request.CreateAdminRequest
import ru.sonso.entity.AdminEntity
import ru.sonso.enumerable.AdminRole
import ru.sonso.mapper.toAdminEntity
import ru.sonso.mapper.toAdminFull
import ru.sonso.repository.AdminRepository
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class AdminAdminsService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getAllAdmins(): List<AdminFull> {
        logger.info("Fetching all admins")
        ensureSuperadmin()
        return adminRepository.findAll()
            .sortedBy { it.createdAt }
            .map { it.toAdminFull() }
            .also { logger.info("Admins fetched, count={}", it.size) }
    }

    @Transactional
    fun createAdmin(request: CreateAdminRequest): AdminFull {
        logger.info("Creating admin, email={}", request.email)
        ensureSuperadmin()
        validateCreateRequest(request)
        if (adminRepository.findByEmailIgnoreCase(request.email.trim()) != null) {
            logger.warn("Admin creation conflict, email={}", request.email)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Admin with this email already exists")
        }

        return adminRepository.save(
            request.toAdminEntity(
                passwordHash = passwordEncoder.encode(request.password),
                role = AdminRole.ADMIN,
            )
        )
            .toAdminFull()
            .also { logger.info("Admin created, id={}", it.id) }
    }

    @Transactional
    fun deleteAdmin(id: UUID): SuccessResponse {
        logger.info("Deleting admin, id={}", id)
        val currentAdmin = ensureSuperadmin()
        val target = adminRepository.findById(id).orElseThrow {
            logger.warn("Admin not found for delete, id={}", id)
            NoSuchElementException("Admin not found")
        }

        if (target.role == AdminRole.SUPERADMIN) {
            logger.warn("Attempt to delete superadmin, id={}", id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Superadmin cannot be deleted")
        }
        if (currentAdmin.id == target.id) {
            logger.warn("Attempt to delete self, id={}", id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete yourself")
        }

        adminRepository.delete(target)
        logger.info("Admin deleted, id={}", id)
        return SuccessResponse(success = true)
    }

    private fun validateCreateRequest(request: CreateAdminRequest) {
        if (request.firstName.isBlank()) throw IllegalArgumentException("first_name is required")
        if (request.lastName.isBlank()) throw IllegalArgumentException("last_name is required")
        if (request.email.isBlank()) throw IllegalArgumentException("email is required")
        if (request.password.length < 6) throw IllegalArgumentException("password length must be >= 6")
    }

    private fun ensureSuperadmin(): AdminEntity {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? AdminEntity
        if (principal?.role != AdminRole.SUPERADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Superadmin access required")
        }
        return principal
    }
}
