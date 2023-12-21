package com.stealthcopter.networktools

import org.junit.Assert
import org.junit.Test

/**
 * Created by matthew on 03/11/17.
 */
class MACToolsTest {
    val invalidMACAddresses: Array<String?>
        get() = arrayOf(null, "beepbeep", "nope", "hello", "00-15-E9-2B-99+3C", "0G-15-E9-2B-99-3C")
    val validMACAddresses: Array<String>
        get() = arrayOf(
            "00:00:00:00:00:00",
            "00-15-E9-2B-99-3C",
            "00:15:E9:2B:99:3C",
            "00-15-e9-2b-99-3c"
        )

    @Test
    fun testValidMACAddresses() {
        for (macAddress in validMACAddresses) {
            Assert.assertTrue(MACTools.isValidMACAddress(macAddress))
        }
    }

    @Test
    fun testInvalidMACAddresses() {
        for (macAddress in invalidMACAddresses) {
            Assert.assertFalse(MACTools.isValidMACAddress(macAddress))
        }
    }
}