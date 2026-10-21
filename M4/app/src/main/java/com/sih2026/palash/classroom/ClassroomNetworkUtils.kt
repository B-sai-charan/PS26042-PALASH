package com.sih2026.palash.classroom

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Utility for determining local device IPv4 network addresses for classroom sharing.
 * Excludes loopback (127.0.0.1) and link-local (169.254.x.x) addresses.
 */
object ClassroomNetworkUtils {

    /**
     * Resolves the current usable local IPv4 address (e.g. Wi-Fi Hotspot or local WLAN IP).
     * Never returns 127.0.0.1 or link-local addresses.
     * Returns null if no active non-loopback IPv4 network interface is available.
     */
    fun getLocalIPv4Address(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (!networkInterface.isUp || networkInterface.isLoopback) {
                    continue
                }
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                        val hostAddress = addr.hostAddress ?: continue
                        if (hostAddress != "127.0.0.1") {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Network interface access exception handled gracefully
        }
        return null
    }
}
