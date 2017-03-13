package com.stealthcopter.networktools;

import java.util.regex.Pattern;

/**
 * Created by mat on 14/12/15.
 */
public class IPTools {

//    public void findDevicesOnSubnet(){
//        // TODO: provide Ip address
//
//        // TODO: Cheat by looking at ARP Cache for a headstart.
//
//        // TODO: Ping devices on network
//
//        // TODO Results.
//    }
//
//    public void getLocalIpAddress(){
//
//    }


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

    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6HexCompressedAddress(final String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }


}
