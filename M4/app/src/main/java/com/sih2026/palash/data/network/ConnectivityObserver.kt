package com.sih2026.palash.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class NetworkConnectivityState {
    ONLINE,
    OFFLINE,
    UNKNOWN
}

/**
 * Lightweight abstraction for monitoring Android device network interfaces.
 * ONLINE indicates network connectivity interface is active, NOT that the backend is healthy.
 */
interface ConnectivityObserver {
    val status: Flow<NetworkConnectivityState>
    fun getCurrentState(): NetworkConnectivityState
}

class AndroidConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun getCurrentState(): NetworkConnectivityState {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return NetworkConnectivityState.OFFLINE
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkConnectivityState.OFFLINE
            val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (isConnected) NetworkConnectivityState.ONLINE else NetworkConnectivityState.OFFLINE
        } catch (e: Exception) {
            NetworkConnectivityState.UNKNOWN
        }
    }

    override val status: Flow<NetworkConnectivityState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkConnectivityState.ONLINE)
            }

            override fun onLost(network: Network) {
                trySend(NetworkConnectivityState.OFFLINE)
            }

            override fun onUnavailable() {
                trySend(NetworkConnectivityState.OFFLINE)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
            trySend(getCurrentState())
        } catch (e: Exception) {
            trySend(NetworkConnectivityState.UNKNOWN)
        }

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore unregister errors on tear down
            }
        }
    }.distinctUntilChanged()
}
