package com.stealthcopter.networktools

import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class PortScanTest {
    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidTimout() {
        PortScan.onAddress("127.0.0.1").setTimeOutMillis(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidPortLow() {
        PortScan.onAddress("127.0.0.1").setPort(0)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidPortHigh() {
        PortScan.onAddress("127.0.0.1").setPort(65536)
    }
}