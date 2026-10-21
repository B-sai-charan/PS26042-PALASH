package com.sih2026.palash.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * States representing the status of background content synchronization.
 */
enum class SyncStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILED,
    OFFLINE,
    CONTRACT_NOT_AVAILABLE
}

/**
 * Contract for background synchronization manager.
 */
interface SyncManager {
    val syncStatus: StateFlow<SyncStatus>
    suspend fun triggerSync(): SyncStatus
}

/**
 * Default implementation of [SyncManager].
 * Safely reports CONTRACT_NOT_AVAILABLE or OFFLINE without attempting invalid requests.
 */
class SyncManagerImpl(
    private val connectivityObserver: ConnectivityObserver? = null
) : SyncManager {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    /**
     * M1 API contracts missing for lessons, worksheets, flashcards, etc.
     */
    private val isContractAvailable = false

    override suspend fun triggerSync(): SyncStatus {
        if (!NetworkConfig.isConfigured) {
            _syncStatus.value = SyncStatus.CONTRACT_NOT_AVAILABLE
            return SyncStatus.CONTRACT_NOT_AVAILABLE
        }

        if (connectivityObserver != null && connectivityObserver.getCurrentState() == NetworkConnectivityState.OFFLINE) {
            _syncStatus.value = SyncStatus.OFFLINE
            return SyncStatus.OFFLINE
        }

        if (!isContractAvailable) {
            _syncStatus.value = SyncStatus.CONTRACT_NOT_AVAILABLE
            return SyncStatus.CONTRACT_NOT_AVAILABLE
        }

        // Future sync logic goes here once M1 contracts are verified
        _syncStatus.value = SyncStatus.IDLE
        return SyncStatus.IDLE
    }
}
