package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sonso.entity.AdminEntity
import ru.sonso.enumerable.AdminRole
import ru.sonso.properties.SuperadminProperties
import ru.sonso.repository.AdminRepository

@Component
class SuperadminInitializer(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val superadminProperties: SuperadminProperties,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        val existingSuperadmins = adminRepository.findAllByRole(AdminRole.SUPERADMIN)

        if (existingSuperadmins.size > 1) {
            logger.error("Invalid state: more than one superadmin exists, count={}", existingSuperadmins.size)
            throw IllegalStateException("There must be only one superadmin")
        }

        if (existingSuperadmins.isNotEmpty()) {
            logger.info("Superadmin already exists, id={}", existingSuperadmins.first().id)
            return
        }

        val firstName = normalizedValue(superadminProperties.firstName, "first-name")
        val lastName = normalizedValue(superadminProperties.lastName, "last-name")
        val email = normalizedValue(superadminProperties.email, "email")
        val password = superadminProperties.password
        val position = superadminProperties.position?.trim()?.takeIf { it.isNotEmpty() }

        if (password.length < 6) {
            throw IllegalStateException("app.superadmin.password length must be >= 6")
        }
        if (adminRepository.findByEmailIgnoreCase(email) != null) {
            throw IllegalStateException("Cannot create superadmin: admin with email $email already exists")
        }

        try {
            val superadmin = adminRepository.saveAndFlush(
                AdminEntity(
                    firstName = firstName,
                    lastName = lastName,
                    position = position,
                    email = email,
                    passwordHash = passwordEncoder.encode(password),
                    role = AdminRole.SUPERADMIN,
                )
            )
            logger.info("Superadmin created, id={}, email={}", superadmin.id, superadmin.email)
        } catch (ex: DataIntegrityViolationException) {
            val superadmins = adminRepository.findAllByRole(AdminRole.SUPERADMIN)
            if (superadmins.size == 1) {
                logger.info("Superadmin already created concurrently, id={}", superadmins.first().id)
                return
            }

            throw IllegalStateException("There must be only one superadmin", ex)
        }
    }

    private fun normalizedValue(value: String, field: String): String {
        val normalized = value.trim()
        if (normalized.isEmpty()) {
            throw IllegalStateException("app.superadmin.$field must not be blank")
        }
        return normalized
    }
}
