package com.sih2026.palash.data.network

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive Unit Tests for PALASH Network Foundation (Step 14).
 * Ensures safe, offline-first behavior without requiring a live backend server.
 */
class NetworkFoundationTest {

    @Before
    fun setUp() {
        NetworkConfig.reset()
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    fun test1_networkConfiguration_noUrl() {
        assertNull(NetworkConfig.baseUrl)
        assertFalse(NetworkConfig.isConfigured)
        assertNull(NetworkClient.getApiService())
    }

    @Test
    fun test2_networkConfiguration_validUrl() {
        NetworkConfig.setDevelopmentBaseUrl("http://10.0.2.2:8080")
        assertEquals("http://10.0.2.2:8080", NetworkConfig.baseUrl)
        assertTrue(NetworkConfig.isConfigured)
        assertNotNull(NetworkClient.getApiService())
    }

    @Test
    fun test3_connectivityStateMapping() {
        assertEquals(3, NetworkConnectivityState.values().size)
        assertTrue(NetworkConnectivityState.values().contains(NetworkConnectivityState.ONLINE))
        assertTrue(NetworkConnectivityState.values().contains(NetworkConnectivityState.OFFLINE))
        assertTrue(NetworkConnectivityState.values().contains(NetworkConnectivityState.UNKNOWN))
    }

    @Test
    fun test4_networkErrorMapping() {
        val httpError = NetworkResult.HttpError(404, "Not Found")
        val networkUnavailable = NetworkResult.NetworkUnavailable()
        val timeout = NetworkResult.Timeout()
        val contractNotAvailable = NetworkResult.ContractNotAvailable()
        val unknown = NetworkResult.UnknownError(RuntimeException("Crash"))

        assertEquals("Server returned error (Code 404)", NetworkErrorMessage.getMessageForResult(httpError))
        assertEquals(NetworkErrorMessage.INTERNET_UNAVAILABLE, NetworkErrorMessage.getMessageForResult(networkUnavailable))
        assertEquals("Connection timed out", NetworkErrorMessage.getMessageForResult(timeout))
        assertEquals(NetworkErrorMessage.CONTRACT_NOT_AVAILABLE, NetworkErrorMessage.getMessageForResult(contractNotAvailable))
        assertEquals("Network communication error", NetworkErrorMessage.getMessageForResult(unknown))
    }

    @Test
    fun test5_contractNotAvailableBehavior() = runBlocking {
        val repo = HealthCheckRepositoryImpl()
        val result = repo.checkBackendHealth()
        assertTrue(result is NetworkResult.NetworkUnavailable)
        assertEquals(NetworkErrorMessage.CONFIGURATION_NOT_AVAILABLE, (result as NetworkResult.NetworkUnavailable).message)
    }

    @Test
    fun test6_syncManagerOfflineBehavior() = runBlocking {
        val fakeConnectivityObserver = object : ConnectivityObserver {
            override val status: kotlinx.coroutines.flow.Flow<NetworkConnectivityState>
                get() = kotlinx.coroutines.flow.flowOf(NetworkConnectivityState.OFFLINE)
            override fun getCurrentState(): NetworkConnectivityState {
                return NetworkConnectivityState.OFFLINE
            }
        }
        
        NetworkConfig.setDevelopmentBaseUrl("http://10.0.2.2:8080")
        val syncManager = SyncManagerImpl(fakeConnectivityObserver)
        val status = syncManager.triggerSync()
        
        assertEquals(SyncStatus.OFFLINE, status)
        assertEquals(SyncStatus.OFFLINE, syncManager.syncStatus.value)
    }

    @Test
    fun test7_syncManagerMissingContractBehavior() = runBlocking {
        val syncManager = SyncManagerImpl()
        val status = syncManager.triggerSync()
        
        assertEquals(SyncStatus.CONTRACT_NOT_AVAILABLE, status)
        assertEquals(SyncStatus.CONTRACT_NOT_AVAILABLE, syncManager.syncStatus.value)
    }
}
