package ru.sonso.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FinishGameResponse(
    val score: Int,
    val status: String,
    val rank: Int,
)
