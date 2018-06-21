package com.stealthcopter.networktools;

import java.util.regex.Pattern;

public class MACTools {

    private static final Pattern PATTERN_MAC =
            Pattern.compile(
                    "^([0-9A-Fa-f]{2}[\\.:-]){5}([0-9A-Fa-f]{2})$");

    // This class is not to be instantiated
    private MACTools() {
    }


    /**
     * Validates a provided MAC address
     *
     * @param macAddress - the MAC address to check
     * @return - true if it is valid MAC address in IEEE802 format (either hyphen or colon separated)
     * eg: "01:23:45:67:89:AB" or "01-23-45-67-89-AB"
     */
    public static boolean isValidMACAddress(final String macAddress) {
        return macAddress != null && PATTERN_MAC.matcher(macAddress).matches();
    }


    /**
     * Convert a MAC string to bytes
     *
     * @param macStr - MAC string in IEEE802 format (either hyphen or colon separated)
     *               eg: "01:23:45:67:89:AB" or "01-23-45-67-89-AB"
     * @return - MAC formatted in bytes
     * @throws IllegalArgumentException - if mac address is invalid
     */
    public static byte[] getMacBytes(String macStr) throws IllegalArgumentException {

        if (macStr == null) throw new IllegalArgumentException("Mac Address cannot be null");

        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

}