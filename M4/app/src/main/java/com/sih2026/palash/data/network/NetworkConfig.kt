package com.sih2026.palash.data.network

/**
 * Centralized configuration mechanism for the PALASH backend network layer.
 * 
 * Rules:
 * 1. No hardcoded production IP address or secret keys.
 * 2. Application functions 100% offline when no backend URL is configured.
 * 3. Debug builds may configure a local development URL for testing.
 */
object NetworkConfig {
    
    @Volatile
    private var _baseUrl: String? = null

    /**
     * Currently configured base URL (null by default for strict offline-first behavior).
     */
    val baseUrl: String?
        get() = _baseUrl

    /**
     * Returns true only when a non-blank base URL is explicitly configured.
     */
    val isConfigured: Boolean
        get() = !_baseUrl.isNullOrBlank()

    /**
     * Development/Debug helper to configure local server base URL.
     * Pass null or empty string to reset to unconfigured offline state.
     */
    fun setDevelopmentBaseUrl(url: String?) {
        val trimmed = url?.trim()
        _baseUrl = if (trimmed.isNullOrBlank()) null else trimmed
    }

    /**
     * Resets network configuration to unconfigured offline state.
     */
    fun reset() {
        _baseUrl = null
    }
}
