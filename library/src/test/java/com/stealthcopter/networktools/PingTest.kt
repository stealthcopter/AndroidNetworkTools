package com.stealthcopter.networktools

import org.junit.Test

/**
 * Created by matthew on 03/11/17.
 */
class PingTest {
    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidDelay() {
        Ping.onAddress("127.0.0.1").setDelayMillis(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidTimeout() {
        Ping.onAddress("127.0.0.1").setTimeOutMillis(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testIllegalArgumentThrownOnInvalidTimes() {
        Ping.onAddress("127.0.0.1").setTimes(-1)
    }
}