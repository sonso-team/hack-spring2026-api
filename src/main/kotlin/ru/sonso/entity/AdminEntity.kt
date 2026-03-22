package ru.sonso.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.sonso.enumerable.AdminRole
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "admins")
data class AdminEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @field:Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = "",

    @field:Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = "",

    @field:Column(name = "position")
    var position: String? = null,

    @field:Column(name = "email", nullable = false, length = 255)
    var email: String = "",

    @field:Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",

    @field:Enumerated(EnumType.STRING)
    @field:JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @field:Column(name = "role", nullable = false, columnDefinition = "ADMIN_ROLE")
    var role: AdminRole = AdminRole.ADMIN,

    @field:Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @field:Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
): UserDetails {
    constructor() : this(
        id = null,
        firstName = "",
        lastName = "",
        position = null,
        email = "",
        passwordHash = "",
        role = AdminRole.ADMIN,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    )

    override fun getUsername() = this.email
    override fun getPassword() = this.passwordHash
    override fun getAuthorities() = mutableListOf(SimpleGrantedAuthority(role.toString()))
}
