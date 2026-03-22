package ru.sonso.mapper

import ru.sonso.dto.AdminFull
import ru.sonso.dto.AdminShort
import ru.sonso.dto.request.CreateAdminRequest
import ru.sonso.entity.AdminEntity
import ru.sonso.enumerable.AdminRole

fun AdminEntity.toAdminShort(): AdminShort = AdminShort(
    id = checkNotNull(id) { "Admin id is missing" },
    firstName = firstName,
    lastName = lastName,
    role = role.toApiValue(),
)

fun AdminEntity.toAdminFull(): AdminFull = AdminFull(
    id = checkNotNull(id) { "Admin id is missing" },
    firstName = firstName,
    lastName = lastName,
    position = position,
    email = email,
    role = role.toApiValue(),
)

fun CreateAdminRequest.toAdminEntity(passwordHash: String, role: AdminRole = AdminRole.ADMIN): AdminEntity = AdminEntity(
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    position = position?.trim()?.takeIf { it.isNotEmpty() },
    email = email.trim(),
    passwordHash = passwordHash,
    role = role,
)
