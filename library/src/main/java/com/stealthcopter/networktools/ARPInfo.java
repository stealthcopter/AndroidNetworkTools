package com.stealthcopter.networktools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by mat on 09/12/15.
 *
 * Looks at the file at /proc/net/arp to fromIPAddress ip/mac addresses from the cache
 * We assume that the file has this structure:
 *
 * IP address       HW type     Flags       HW address            Mask     Device
 * 192.168.18.11    0x1         0x2         00:04:20:06:55:1a     *        eth0
 * 192.168.18.36    0x1         0x2         00:22:43:ab:2a:5b     *        eth0
 *
 * Also looks at the output from `ip sleigh show` command
 *
 */
public class ARPInfo {

    // This class is not to be instantiated
    private ARPInfo() {
    }


    /**
     * Try to extract a hardware MAC address from a given IP address
     *
     * @param ip - IP address to search for
     * @return the MAC from the ARP cache or null in format "01:23:45:67:89:ab"
     */
    public static String getMACFromIPAddress(String ip) {
        if (ip == null) {
            return null;
        }

        HashMap<String, String> cache = getAllIPAndMACAddressesInARPCache();
        return cache.get(ip);
    }


    /**
     * Try to extract a IP address from the given MAC address
     *
     * @param macAddress in format "01:23:45:67:89:ab" to search for
     * @return the IP address found or null in format "192.168.0.1"
     */
    public static String getIPAddressFromMAC(String macAddress) {
        if (macAddress == null) {
            return null;
        }

        if (!macAddress.matches("..:..:..:..:..:..")) {
            throw new IllegalArgumentException("Invalid MAC Address");
        }

        HashMap<String, String> cache = getAllIPAndMACAddressesInARPCache();
        for (String ip : cache.keySet()) {
            if (cache.get(ip).equalsIgnoreCase(macAddress)) {
                return ip;
            }
        }
        return null;
    }


    /**
     * Returns all the ip addresses currently in the ARP cache (/proc/net/arp).
     *
     * @return list of IP addresses found
     */
    public static ArrayList<String> getAllIPAddressesInARPCache() {
        return new ArrayList<>(getAllIPAndMACAddressesInARPCache().keySet());
    }

    /**
     * Returns all the MAC addresses currently in the ARP cache (/proc/net/arp).
     *
     * @return list of MAC addresses found
     */
    public static ArrayList<String> getAllMACAddressesInARPCache() {
        return new ArrayList<>(getAllIPAndMACAddressesInARPCache().values());
    }


    /**
     * Returns all the IP/MAC address pairs currently in the following places
     *
     * 1. ARP cache (/proc/net/arp).
     * 2. `ip neigh show` command
     *
     * @return list of IP/MAC address pairs found
     */
    public static HashMap<String, String> getAllIPAndMACAddressesInARPCache() {
        HashMap<String, String> macList = getAllIPandMACAddressesFromIPSleigh();
        for (String line : getLinesInARPCache()) {
            String[] splitted = line.split(" +");
            if (splitted.length >= 4) {
                // Ignore values with invalid MAC addresses
                if (splitted[3].matches("..:..:..:..:..:..")
                        && !splitted[3].equals("00:00:00:00:00:00")) {
                    if (!macList.containsKey(splitted[0])) {
                        macList.put(splitted[0], splitted[3]);
                    }
                }
            }
        }
        return macList;
    }

    /**
     * Method to read lines from the ARP Cache
     *
     * @return the lines of the ARP Cache.
     */
    private static ArrayList<String> getLinesInARPCache() {
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


    /**
     * Get the IP / MAC address pairs from `ip sleigh show` command
     *
     * @return hashmap of ips and mac addresses
     */
    private static HashMap<String, String> getAllIPandMACAddressesFromIPSleigh() {
        HashMap<String, String> macList = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("ip neigh show");
            proc.waitFor();
            int exit = proc.exitValue();

            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line = buffer.readLine()) != null) {
                String[] splits = line.split(" ");
                if (splits.length < 4) {
                    continue;
                }
                macList.put(splits[0], splits[4]);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return macList;
    }

}
