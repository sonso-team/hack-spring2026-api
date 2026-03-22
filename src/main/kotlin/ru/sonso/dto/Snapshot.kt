package ru.sonso.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Snapshot(
    val score: Int,
    val timestamp: Long,
    val kills: Int,
)
