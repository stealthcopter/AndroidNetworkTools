package com.stealthcopter.networktools;

import android.support.annotation.NonNull;

import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.subnet.Device;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by mat on 03/11/17.
 */
public class SubnetDevices {
    private int noThreads = 255;

    private ArrayList<String> addresses;
    private ArrayList<Device> devicesFound;
    private OnSubnetDeviceFound listener;

    // This class is not to be instantiated
    private SubnetDevices() {
    }

    public interface OnSubnetDeviceFound {
        void onDeviceFound(Device device);
        void onFinished(ArrayList<Device> devicesFound);
    }

    /**
     * Find devices on the subnet working from the local device ip address
     */
    public static SubnetDevices fromLocalAddress() {
        InetAddress ipv4 = IPTools.getLocalIPv4Address();

        if (ipv4 == null){
            throw new IllegalAccessError("Could not access local ip address");
        }

        return fromIPAddress(ipv4.getHostAddress());
    }

    /**
     * @param inetAddress - an ip address in the subnet
     */
    public static SubnetDevices fromIPAddress(@NonNull InetAddress inetAddress) {
        return fromIPAddress(inetAddress.getHostAddress());
    }

    /**
     * @param ipAddress - the ipAddress string of any device in the subnet i.e. "192.168.0.1"
     *                  the final part will be ignored
     */
    public static SubnetDevices fromIPAddress(@NonNull final String ipAddress) {

        if (!IPTools.isIPv4Address(ipAddress)){
            throw new IllegalArgumentException("Invalid IP Address");
        }

        SubnetDevices subnetDevice = new SubnetDevices();

        subnetDevice.addresses = new ArrayList<>();

        // Get addresses from ARP Info first as they are likely to be pingable
        subnetDevice.addresses.addAll(ARPInfo.getAllIPAddressesInARPCache());

        // Add all missing addresses in subnet
        for (int j = 0; j < 255; j++) {
            if (!subnetDevice.addresses.contains(ipAddress + j)) {
                subnetDevice.addresses.add(ipAddress + j);
            }
        }

        return subnetDevice;

    }

    /**
     *
     * @param noThreads set the number of threads to work with, note we default to a large number
     *                  as these requests are network heavy not cpu heavy.
     * @return self
     * @throws IllegalAccessException
     */
    public SubnetDevices setNoThreads(int noThreads) throws IllegalAccessException {
        if (noThreads < 1) throw new IllegalArgumentException("Cannot have less than 1 thread");
        this.noThreads = noThreads;
        return this;
    }

    public void findDevices(final OnSubnetDeviceFound listener) {

        this.listener = listener;

        devicesFound = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(this.noThreads);

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

        this.listener.onFinished(devicesFound);
    }

    private synchronized void subnetDeviceFound(Device device){
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
            try {
                InetAddress ia = InetAddress.getByName(address);
                PingResult pingResult = Ping.onAddress(ia).doPing();
                if (pingResult.isReachable) {
                    Device device = new Device(ia);
                    device.mac = ARPInfo.getMACFromIPAddress(ia.getHostAddress());
                    device.time = pingResult.timeTaken;
                    subnetDeviceFound(device);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

}