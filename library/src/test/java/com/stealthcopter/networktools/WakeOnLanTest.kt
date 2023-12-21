package com.stealthcopter.networktools

import org.junit.Test
import java.net.InetAddress

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class WakeOnLanTest {
    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidIPaddressStr() {
        WakeOnLan.sendWakeOnLan(null as String?, "00:04:20:06:55:1a", 9, 10000, 5)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidIPaddress() {
        WakeOnLan.sendWakeOnLan(null as InetAddress?, "00:04:20:06:55:1a", 9, 10000, 5)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidMACaddress() {
        WakeOnLan.sendWakeOnLan("192.168.0.1", null, 9, 10000, 5)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidPortLow() {
        WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", -1, 10000, 5)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidPortHigh() {
        WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", 65536, 10000, 5)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidPackets() {
        WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", 9, 10000, 0)
    }
}