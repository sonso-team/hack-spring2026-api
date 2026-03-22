package ru.sonso.dto.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RegisterPlayerRequest(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val inviteCode: String,
)
