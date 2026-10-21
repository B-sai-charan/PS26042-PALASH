package com.sih2026.palash.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * M1 Backend Health Check Response DTO.
 * Contract:
 * {
 *   "status": "ok",
 *   "service": "palash-backend",
 *   "version": "0.1.0"
 * }
 */
data class HealthResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("service") val service: String,
    @SerializedName("version") val version: String
)
