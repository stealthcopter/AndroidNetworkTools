package com.stealthcopter.networktools

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.regex.Pattern

object IPTools {
    /**
     * Ip matching patterns from
     * https://examples.javacodegeeks.com/core-java/util/regex/regular-expressions-for-ip-v4-and-ip-v6-addresses/
     * note that these patterns will match most but not all valid ips
     */
    private val IPV4_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
    )
    private val IPV6_STD_PATTERN = Pattern.compile(
        "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    )
    private val IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile(
        "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$"
    )

    @JvmStatic
    fun isIPv4Address(address: String?): Boolean {
        return address != null && IPV4_PATTERN.matcher(address).matches()
    }

    fun isIPv6StdAddress(address: String?): Boolean {
        return address != null && IPV6_STD_PATTERN.matcher(address).matches()
    }

    fun isIPv6HexCompressedAddress(address: String?): Boolean {
        return address != null && IPV6_HEX_COMPRESSED_PATTERN.matcher(address).matches()
    }

    fun isIPv6Address(address: String?): Boolean {
        return address != null && (isIPv6StdAddress(address) || isIPv6HexCompressedAddress(address))
    }

    /**
     * @return The first local IPv4 address, or null
     */
    @JvmStatic
    val localIPv4Address: InetAddress?
        get() {
            val localAddresses = localIPv4Addresses
            return if (localAddresses.size > 0) localAddresses[0] else null
        }

    /**
     * @return The list of all IPv4 addresses found
     */
    val localIPv4Addresses: ArrayList<InetAddress>
        get() {
            val foundAddresses = ArrayList<InetAddress>()
            val ifaces: Enumeration<NetworkInterface>
            try {
                ifaces = NetworkInterface.getNetworkInterfaces()
                while (ifaces.hasMoreElements()) {
                    val iface = ifaces.nextElement()
                    val addresses = iface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (addr is Inet4Address && !addr.isLoopbackAddress()) {
                            foundAddresses.add(addr)
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
            return foundAddresses
        }

    /**
     * Check if the provided ip address refers to the localhost
     *
     * https://stackoverflow.com/a/2406819/315998
     *
     * @param addr - address to check
     * @return - true if ip address is self
     */
    @JvmStatic
    fun isIpAddressLocalhost(addr: InetAddress?): Boolean {
        if (addr == null) return false

        // Check if the address is a valid special local or loop back
        return if (addr.isAnyLocalAddress || addr.isLoopbackAddress) true else try {
            NetworkInterface.getByInetAddress(addr) != null
        } catch (e: SocketException) {
            false
        }

        // Check if the address is defined on any interface
    }

    /**
     * Check if the provided ip address refers to the localhost
     *
     * https://stackoverflow.com/a/2406819/315998
     *
     * @param addr - address to check
     * @return - true if ip address is self
     */
    @JvmStatic
    fun isIpAddressLocalNetwork(addr: InetAddress?): Boolean {
        return addr != null && addr.isSiteLocalAddress
    }
}