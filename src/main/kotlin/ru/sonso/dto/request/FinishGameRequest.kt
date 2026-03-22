package ru.sonso.dto.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import ru.sonso.dto.Snapshot

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FinishGameRequest(
    val sessionToken: String,
    val finalScore: Int,
    val snapshots: List<Snapshot>
)
