package com.sih2026.palash.data.network

/**
 * Reusable network result abstraction preventing raw HTTP exceptions from leaking into UI.
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkUnavailable(val message: String = "Internet connection unavailable") : NetworkResult<Nothing>()
    data class Timeout(val message: String = "Request timed out") : NetworkResult<Nothing>()
    data class ContractNotAvailable(val message: String = "Backend API contract not available") : NetworkResult<Nothing>()
    data class UnknownError(val exception: Throwable) : NetworkResult<Nothing>()
}
