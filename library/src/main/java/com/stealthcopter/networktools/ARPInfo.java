package com.stealthcopter.networktools;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mat on 09/12/15.
 *
 * Looks at the file at /proc/net/arp to find ip/mac addresses from the cache
 * We assume that the file has this structure:
 *
 * IP address       HW type     Flags       HW address            Mask     Device
 * 192.168.18.11    0x1         0x2         00:04:20:06:55:1a     *        eth0
 * 192.168.18.36    0x1         0x2         00:22:43:ab:2a:5b     *        eth0
 *
 */
public class ARPInfo {

    /**
     * Try to extract a hardware MAC address from a given IP address using the
     * ARP cache (/proc/net/arp).
     *
     * @param ip - IP address to search for
     * @return the MAC from the ARP cache or null in format "01:23:45:67:89:ab"
     */
    @Nullable public static String getMACFromIPAddress(String ip) {
        if (ip == null)
            return null;

        for(String line : getLinesInARPCache()) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                String mac = splitted[3];
                if (mac.matches("..:..:..:..:..:..")) {
                    return mac;
                } else {
                    return null;
                }
            }
        }
        return null;
    }


    /**
     * Try to extract a IP address from the given MAC address using the
     * ARP cache (/proc/net/arp).
     *
     * @param macAddress in format "01:23:45:67:89:ab" to search for
     * @return the IP address found or null in format "192.168.0.1"
     */
    @Nullable public static String getIPAddressFromMAC(String macAddress) {
        if (macAddress == null)
            return null;

        for(String line : getLinesInARPCache()) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4 && macAddress.equals(splitted[3])) {
                String ipAddress = splitted[0];
                return ipAddress;
            }
        }
        return null;
    }

    /**
     * Returns all the ip addresses currently in the ARP cache (/proc/net/arp).
     *
     * @return list of IP addresses found
     */
    public ArrayList<String> getAllIPAddressesInARPCache(){
        ArrayList<String> ipList = new ArrayList<>();
        for(String line : getLinesInARPCache()) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                ipList.add(splitted[0]);
            }
        }
        return ipList;
    }

    /**
     * Returns all the MAC addresses currently in the ARP cache (/proc/net/arp).
     *
     * @return list of MAC addresses found
     */
    public ArrayList<String> getAllMACAddressesInARPCache(){
        ArrayList<String> macList = new ArrayList<>();
        for(String line : getLinesInARPCache()) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                macList.add(splitted[3]);
            }
        }
        return macList;
    }


    /**
     * Returns all the IP/MAC address pairs currently in the ARP cache (/proc/net/arp).
     *
     * @return list of IP/MAC address pairs found
     */
    public ArrayList<Pair<String, String>> getAllIPAndMACAddressesInARPCache(){
        ArrayList<Pair<String, String>> macList = new ArrayList<>();
        for(String line : getLinesInARPCache()) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                macList.add(new Pair<>(splitted[0], splitted[3]));
            }
        }
        return macList;
    }

    /**
     * Method to read lines from the ARP Cache
     * @return the lines of the ARP Cache.
     */
    private static ArrayList<String> getLinesInARPCache(){
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

}
