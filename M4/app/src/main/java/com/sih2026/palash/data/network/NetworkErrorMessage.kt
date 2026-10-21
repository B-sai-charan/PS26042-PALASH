package com.sih2026.palash.data.network

/**
 * Reusable user-safe messaging utility for network states and errors.
 * Ensures zero technical stack traces or raw HTTP exceptions are exposed to users.
 */
object NetworkErrorMessage {
    const val OFFLINE_MODE_ACTIVE = "Offline mode active"
    const val INTERNET_UNAVAILABLE = "Internet connection unavailable"
    const val CONFIGURATION_NOT_AVAILABLE = "Backend configuration not available"
    const val CONTRACT_NOT_AVAILABLE = "Backend API contract not available"
    const val UNABLE_TO_SYNC = "Unable to sync content"
    const val SERVER_UNREACHABLE = "Backend server unreachable"

    fun getMessageForResult(result: NetworkResult<*>): String {
        return when (result) {
            is NetworkResult.Success -> ""
            is NetworkResult.HttpError -> "Server returned error (Code ${result.code})"
            is NetworkResult.NetworkUnavailable -> INTERNET_UNAVAILABLE
            is NetworkResult.Timeout -> "Connection timed out"
            is NetworkResult.ContractNotAvailable -> CONTRACT_NOT_AVAILABLE
            is NetworkResult.UnknownError -> "Network communication error"
        }
    }
}
