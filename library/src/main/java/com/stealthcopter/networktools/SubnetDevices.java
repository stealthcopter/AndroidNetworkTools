package com.stealthcopter.networktools;

import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.subnet.Device;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SubnetDevices {
    private int noThreads = 100;

    private ArrayList<String> addresses;
    private ArrayList<Device> devicesFound;
    private OnSubnetDeviceFound listener;
    private int timeOutMillis = 2500;
    private boolean cancelled = false;

    private boolean disableProcNetMethod = false;
    private HashMap<String, String> ipMacHashMap = null;

    // This class is not to be instantiated
    private SubnetDevices() {
    }

    public interface OnSubnetDeviceFound {
        void onDeviceFound(Device device);

        void onFinished(ArrayList<Device> devicesFound);
    }

    /**
     * Find devices on the subnet working from the local device ip address
     *
     * @return - this for chaining
     */
    public static SubnetDevices fromLocalAddress() {
        InetAddress ipv4 = IPTools.getLocalIPv4Address();

        if (ipv4 == null) {
            throw new IllegalAccessError("Could not access local ip address");
        }

        return fromIPAddress(ipv4.getHostAddress());
    }

    /**
     * @param inetAddress - an ip address in the subnet
     *
     * @return - this for chaining
     */
    public static SubnetDevices fromIPAddress(InetAddress inetAddress) {
        return fromIPAddress(inetAddress.getHostAddress());
    }

    /**
     * @param ipAddress - the ipAddress string of any device in the subnet i.e. "192.168.0.1"
     *                  the final part will be ignored
     *
     * @return - this for chaining
     */
    public static SubnetDevices fromIPAddress(final String ipAddress) {

        if (!IPTools.isIPv4Address(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP Address");
        }

        String segment = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1);

        SubnetDevices subnetDevice = new SubnetDevices();

        subnetDevice.addresses = new ArrayList<>();

        // Get addresses from ARP Info first as they are likely to be reachable
        for(String ip : ARPInfo.getAllIPAddressesInARPCache()) {
            if (ip.startsWith(segment)) {
                subnetDevice.addresses.add(ip);
            }
        }

        // Add all missing addresses in subnet
        for (int j = 0; j < 255; j++) {
            if (!subnetDevice.addresses.contains(segment + j)) {
                subnetDevice.addresses.add(segment + j);
            }
        }

        return subnetDevice;

    }


    /**
     * @param ipAddresses - the ipAddresses of devices to be checked
     *
     * @return - this for chaining
     */
    public static SubnetDevices fromIPList(final List<String> ipAddresses) {

        SubnetDevices subnetDevice = new SubnetDevices();

        subnetDevice.addresses = new ArrayList<>();

        subnetDevice.addresses.addAll(ipAddresses);

        return subnetDevice;

    }

    /**
     * @param noThreads set the number of threads to work with, note we default to a large number
     *                  as these requests are network heavy not cpu heavy.
     *
     * @throws IllegalArgumentException - if invalid number of threads requested
     *
     * @return - this for chaining
     */
    public SubnetDevices setNoThreads(int noThreads) throws IllegalArgumentException {
        if (noThreads < 1) throw new IllegalArgumentException("Cannot have less than 1 thread");
        this.noThreads = noThreads;
        return this;
    }

    /**
     * Sets the timeout for each address we try to ping
     *
     * @param timeOutMillis - timeout in milliseconds for each ping
     *
     * @return this object to allow chaining
     *
     * @throws IllegalArgumentException - if timeout is less than zero
     */
    public SubnetDevices setTimeOutMillis(int timeOutMillis) throws IllegalArgumentException {
        if (timeOutMillis < 0) throw new IllegalArgumentException("Timeout cannot be less than 0");
        this.timeOutMillis = timeOutMillis;
        return this;
    }

    /**
     *
     * @param disable if set to true we will not attempt to read from /proc/net/arp
     *                directly. This avoids any Android 10 permissions logs appearing.
     */
    public void setDisableProcNetMethod(boolean disable) {
        this.disableProcNetMethod = disableProcNetMethod;
    }

    /**
     * Cancel a running scan
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Starts the scan to find other devices on the subnet
     *
     * @param listener - to pass on the results
     * @return this object so we can call cancel on it if needed
     */
    public SubnetDevices findDevices(final OnSubnetDeviceFound listener) {

        this.listener = listener;

        cancelled = false;
        devicesFound = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Load mac addresses into cache var (to avoid hammering the /proc/net/arp file when
                // lots of devices are found on the network.
                ipMacHashMap = disableProcNetMethod ? ARPInfo.getAllIPandMACAddressesFromIPSleigh() : ARPInfo.getAllIPAndMACAddressesInARPCache();

                ExecutorService executor = Executors.newFixedThreadPool(noThreads);

                for (final String add : addresses) {
                    Runnable worker = new SubnetDeviceFinderRunnable(add);
                    executor.execute(worker);
                }

                // This will make the executor accept no new threads
                // and finish all existing threads in the queue
                executor.shutdown();
                // Wait until all threads are finish
                try {
                    executor.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Loop over devices found and add in the MAC addresses if missing.
                // We do this after scanning for all devices as /proc/net/arp may add info
                // because of the scan.
                ipMacHashMap = disableProcNetMethod ? ARPInfo.getAllIPandMACAddressesFromIPSleigh() : ARPInfo.getAllIPAndMACAddressesInARPCache();
                for (Device device : devicesFound) {
                    if (device.mac == null && ipMacHashMap.containsKey(device.ip)) {
                        device.mac = ipMacHashMap.get(device.ip);
                    }
                }


                listener.onFinished(devicesFound);

            }
        }).start();

        return this;
    }

    private synchronized void subnetDeviceFound(Device device) {
        devicesFound.add(device);
        listener.onDeviceFound(device);
    }

    public class SubnetDeviceFinderRunnable implements Runnable {
        private final String address;

        SubnetDeviceFinderRunnable(String address) {
            this.address = address;
        }

        @Override
        public void run() {

            if (cancelled) return;

            try {
                InetAddress ia = InetAddress.getByName(address);
                PingResult pingResult = Ping.onAddress(ia).setTimeOutMillis(timeOutMillis).doPing();
                if (pingResult.isReachable) {
                    Device device = new Device(ia);

                    // Add the device MAC address if it is in the cache
                    if (ipMacHashMap.containsKey(ia.getHostAddress())) {
                        device.mac = ipMacHashMap.get(ia.getHostAddress());
                    }

                    device.time = pingResult.timeTaken;
                    subnetDeviceFound(device);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

}