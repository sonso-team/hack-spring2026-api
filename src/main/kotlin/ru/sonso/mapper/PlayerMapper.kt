package ru.sonso.mapper

import ru.sonso.dto.request.RegisterPlayerRequest
import ru.sonso.entity.PlayerEntity
import java.time.OffsetDateTime
import java.util.UUID

fun RegisterPlayerRequest.toPlayerEntity(lobbyId: UUID): PlayerEntity = PlayerEntity(
    lobbyId = lobbyId,
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    phone = phone.trim(),
    email = email.trim(),
    createdAt = OffsetDateTime.now(),
)
