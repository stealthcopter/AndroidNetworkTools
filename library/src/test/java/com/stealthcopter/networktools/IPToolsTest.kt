package com.stealthcopter.networktools

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Created by matthew on 03/11/17.
 */
class IPToolsTest {
    val invalidIpAddresses: Array<String?>
        get() = arrayOf(null, "beepbeep", "nope", "hello")
    val iPv4Addresses: Array<String?>
        get() = arrayOf("192.168.0.1", "127.0.0.1", "10.0.0.1")
    val iPv6Addresses: Array<String?>
        get() = arrayOf("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
    val iPv6AddressesHexCompresed: Array<String>
        get() = arrayOf(
            "2001:db8:85a3::8a2e:370:7334",
            "2001::8a2e:370:7334",
            "2001:",
            "2001::2001"
        )

    @Test
    fun testIsIPv4Address() {
        assertIPv4Address(iPv4Addresses, true)
        assertIPv4Address(iPv6Addresses, false)
        assertIPv4Address(invalidIpAddresses, false)
    }

    @Test
    fun testIsIPv6Address() {
        assertIPv6Address(iPv4Addresses, false)
        assertIPv6Address(iPv6Addresses, true)
        assertIPv6Address(invalidIpAddresses, false)
    }

    @Test
    fun testIsIPv6AddressesStandard() {
        assertIPv6StdAddress(iPv4Addresses, false)
        assertIPv6StdAddress(iPv6Addresses, true)
        assertIPv6StdAddress(invalidIpAddresses, false)
    }

    @Test
    @Ignore // Recheck this, either test is broken or regex is wrong
    fun testIPv6HexCompressedAddress() {
        for (address in iPv6AddressesHexCompresed) {
            Assert.assertTrue(IPTools.isIPv6HexCompressedAddress(address))
        }
    }

    @Test
    fun testGetLocalAddressReturnsLocalIP() {
        val test = IPTools.localIPv4Address
        Assert.assertNotNull(test)
        Assert.assertTrue(IPTools.isIpAddressLocalhost(test))
        Assert.assertTrue(IPTools.isIpAddressLocalNetwork(test))
    }

    @Test
    fun testGetAllLocalAddressReturnsLocalIP() {
        val test: List<InetAddress> = IPTools.localIPv4Addresses
        for (address in test) {
            println(address)
            Assert.assertNotNull(address)
            Assert.assertTrue(IPTools.isIpAddressLocalhost(address))
            Assert.assertTrue(IPTools.isIpAddressLocalNetwork(address))
        }
    }

    @Test
    @Throws(UnknownHostException::class)
    fun testLocalAddresses() {
        Assert.assertTrue(IPTools.isIpAddressLocalhost(InetAddress.getByName("127.0.0.1")))
        Assert.assertFalse(IPTools.isIpAddressLocalhost(InetAddress.getByName("8.8.8.8")))
    }

    @Test
    @Throws(UnknownHostException::class)
    fun testLocalAddressesNetwork() {
        Assert.assertFalse(IPTools.isIpAddressLocalNetwork(InetAddress.getByName("8.8.8.8")))
    }

    private fun assertIPv4Address(ips: Array<String?>, isIPv4Address: Boolean) {
        for (address in ips) {
            Assert.assertThat(IPTools.isIPv4Address(address), CoreMatchers.`is`(isIPv4Address))
        }
    }

    private fun assertIPv6Address(ips: Array<String?>, isIPv6Address: Boolean) {
        for (address in ips) {
            Assert.assertThat(IPTools.isIPv6Address(address), CoreMatchers.`is`(isIPv6Address))
        }
    }

    private fun assertIPv6StdAddress(ips: Array<String?>, isIPv6StdAddress: Boolean) {
        for (address in ips) {
            Assert.assertThat(
                IPTools.isIPv6StdAddress(address),
                CoreMatchers.`is`(isIPv6StdAddress)
            )
        }
    }
}