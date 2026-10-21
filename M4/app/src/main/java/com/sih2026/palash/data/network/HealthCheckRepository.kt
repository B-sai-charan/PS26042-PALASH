package com.sih2026.palash.data.network

/**
 * Repository interface for checking backend service health status.
 */
interface HealthCheckRepository {
    suspend fun checkBackendHealth(): NetworkResult<Unit>
}

/**
 * Implementation of [HealthCheckRepository].
 * Safe for offline operation and connects to verified M1 GET /health API.
 */
class HealthCheckRepositoryImpl(
    private val connectivityObserver: ConnectivityObserver? = null
) : HealthCheckRepository {

    override suspend fun checkBackendHealth(): NetworkResult<Unit> {
        if (!NetworkConfig.isConfigured) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)
        }

        if (connectivityObserver != null && connectivityObserver.getCurrentState() == NetworkConnectivityState.OFFLINE) {
            return NetworkResult.NetworkUnavailable(NetworkErrorMessage.INTERNET_UNAVAILABLE)
        }

        val apiService = NetworkClient.getApiService()
            ?: return NetworkResult.NetworkUnavailable(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE)

        return try {
            val response = apiService.checkHealth()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "ok") {
                    NetworkResult.Success(Unit)
                } else {
                    NetworkResult.HttpError(response.code(), response.message().ifBlank { "Health check status not ok" })
                }
            } else {
                NetworkResult.HttpError(response.code(), response.message())
            }
        } catch (e: java.net.SocketTimeoutException) {
            NetworkResult.Timeout()
        } catch (e: java.io.IOException) {
            NetworkResult.NetworkUnavailable(e.message ?: NetworkErrorMessage.SERVER_UNREACHABLE)
        } catch (e: Exception) {
            NetworkResult.UnknownError(e)
        }
    }
}
