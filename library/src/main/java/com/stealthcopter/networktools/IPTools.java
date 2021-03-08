package com.stealthcopter.networktools;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class IPTools {

    /**
     * Ip matching patterns from
     * https://examples.javacodegeeks.com/core-java/util/regex/regular-expressions-for-ip-v4-and-ip-v6-addresses/
     * note that these patterns will match most but not all valid ips
     */

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_STD_PATTERN =
            Pattern.compile(
                    "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
            Pattern.compile(
                    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    // This class is not to be instantiated
    private IPTools() {
    }

    public static boolean isIPv4Address(final String address) {
        return address != null && IPV4_PATTERN.matcher(address).matches();
    }

    public static boolean isIPv6StdAddress(final String address) {
        return address != null && IPV6_STD_PATTERN.matcher(address).matches();
    }

    public static boolean isIPv6HexCompressedAddress(final String address) {
        return address != null && IPV6_HEX_COMPRESSED_PATTERN.matcher(address).matches();
    }

    public static boolean isIPv6Address(final String address) {
        return address != null && (isIPv6StdAddress(address) || isIPv6HexCompressedAddress(address));
    }

    /**
     * @return The first local IPv4 address, or null
     */
    public static InetAddress getLocalIPv4Address() {
        ArrayList<InetAddress> localAddresses = getLocalIPv4Addresses();
        return localAddresses.size() > 0 ? localAddresses.get(0) : null;
    }

    /**
     * @return The list of all IPv4 addresses found
     */
    public static ArrayList<InetAddress> getLocalIPv4Addresses() {

        ArrayList<InetAddress> foundAddresses = new ArrayList<>();

        Enumeration<NetworkInterface> ifaces;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();

            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        foundAddresses.add(addr);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return foundAddresses;
    }


    /**
     * Check if the provided ip address refers to the localhost
     *
     * https://stackoverflow.com/a/2406819/315998
     *
     * @param addr - address to check
     * @return - true if ip address is self
     */
    public static boolean isIpAddressLocalhost(InetAddress addr) {
        if (addr == null) return false;

        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
            return true;

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    /**
     * Check if the provided ip address refers to the localhost
     *
     * https://stackoverflow.com/a/2406819/315998
     *
     * @param addr - address to check
     * @return - true if ip address is self
     */
    public static boolean isIpAddressLocalNetwork(InetAddress addr) {
        return addr != null && addr.isSiteLocalAddress();
    }


}
