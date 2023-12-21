package com.stealthcopter.networktools

import com.stealthcopter.networktools.ARPInfo.allIPAddressesInARPCache
import com.stealthcopter.networktools.ARPInfo.allIPAndMACAddressesInARPCache
import com.stealthcopter.networktools.ARPInfo.allIPandMACAddressesFromIPSleigh
import com.stealthcopter.networktools.IPTools.isIPv4Address
import com.stealthcopter.networktools.IPTools.localIPv4Address
import com.stealthcopter.networktools.Ping.Companion.onAddress
import com.stealthcopter.networktools.subnet.Device
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SubnetDevices  // This class is not to be instantiated
private constructor() {
    private var noThreads = 100
    private var addresses: ArrayList<String>? = null
    private var devicesFound: ArrayList<Device>? = null
    private var listener: OnSubnetDeviceFound? = null
    private var timeOutMillis = 2500
    private var cancelled = false
    private var disableProcNetMethod = false
    private var ipMacHashMap: HashMap<String, String>? = null

    interface OnSubnetDeviceFound {
        fun onDeviceFound(device: Device?)
        fun onFinished(devicesFound: ArrayList<Device>?)
    }

    /**
     * @param noThreads set the number of threads to work with, note we default to a large number
     * as these requests are network heavy not cpu heavy.
     *
     * @throws IllegalArgumentException - if invalid number of threads requested
     *
     * @return - this for chaining
     */
    @Throws(IllegalArgumentException::class)
    fun setNoThreads(noThreads: Int): SubnetDevices {
        require(noThreads >= 1) { "Cannot have less than 1 thread" }
        this.noThreads = noThreads
        return this
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
    @Throws(IllegalArgumentException::class)
    fun setTimeOutMillis(timeOutMillis: Int): SubnetDevices {
        require(timeOutMillis >= 0) { "Timeout cannot be less than 0" }
        this.timeOutMillis = timeOutMillis
        return this
    }

    /**
     *
     * @param disable if set to true we will not attempt to read from /proc/net/arp
     * directly. This avoids any Android 10 permissions logs appearing.
     */
    fun setDisableProcNetMethod(disable: Boolean) {
        disableProcNetMethod = disableProcNetMethod
    }

    /**
     * Cancel a running scan
     */
    fun cancel() {
        cancelled = true
    }

    /**
     * Starts the scan to find other devices on the subnet
     *
     * @param listener - to pass on the results
     * @return this object so we can call cancel on it if needed
     */
    fun findDevices(listener: OnSubnetDeviceFound): SubnetDevices {
        this.listener = listener
        cancelled = false
        devicesFound = ArrayList()
        Thread { // Load mac addresses into cache var (to avoid hammering the /proc/net/arp file when
            // lots of devices are found on the network.
            ipMacHashMap =
                if (disableProcNetMethod) allIPandMACAddressesFromIPSleigh else allIPAndMACAddressesInARPCache
            val executor = Executors.newFixedThreadPool(noThreads)
            for (add in addresses!!) {
                val worker: Runnable = SubnetDeviceFinderRunnable(add)
                executor.execute(worker)
            }

            // This will make the executor accept no new threads
            // and finish all existing threads in the queue
            executor.shutdown()
            // Wait until all threads are finish
            try {
                executor.awaitTermination(1, TimeUnit.HOURS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            // Loop over devices found and add in the MAC addresses if missing.
            // We do this after scanning for all devices as /proc/net/arp may add info
            // because of the scan.
            ipMacHashMap =
                if (disableProcNetMethod) allIPandMACAddressesFromIPSleigh else allIPAndMACAddressesInARPCache
            for (device in devicesFound!!) {
                if (device.mac == null && ipMacHashMap!!.containsKey(device.ip)) {
                    device.mac = ipMacHashMap!![device.ip]
                }
            }
            listener.onFinished(devicesFound)
        }.start()
        return this
    }

    @Synchronized
    private fun subnetDeviceFound(device: Device) {
        devicesFound!!.add(device)
        listener!!.onDeviceFound(device)
    }

    inner class SubnetDeviceFinderRunnable internal constructor(private val address: String?) :
        Runnable {
        override fun run() {
            if (cancelled) return
            try {
                val ia = InetAddress.getByName(address)
                val pingResult = onAddress(ia).setTimeOutMillis(timeOutMillis).doPing()
                if (pingResult.isReachable) {
                    val device = Device(ia)

                    // Add the device MAC address if it is in the cache
                    if (ipMacHashMap!!.containsKey(ia.hostAddress)) {
                        device.mac = ipMacHashMap!![ia.hostAddress]
                    }
                    device.time = pingResult.timeTaken
                    subnetDeviceFound(device)
                }
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        /**
         * Find devices on the subnet working from the local device ip address
         *
         * @return - this for chaining
         */
        fun fromLocalAddress(): SubnetDevices {
            val ipv4 = localIPv4Address
                ?: throw IllegalAccessError("Could not access local ip address")
            return fromIPAddress(ipv4.hostAddress)
        }

        /**
         * @param inetAddress - an ip address in the subnet
         *
         * @return - this for chaining
         */
        fun fromIPAddress(inetAddress: InetAddress): SubnetDevices {
            return fromIPAddress(inetAddress.hostAddress)
        }

        /**
         * @param ipAddress - the ipAddress string of any device in the subnet i.e. "192.168.0.1"
         * the final part will be ignored
         *
         * @return - this for chaining
         */
        fun fromIPAddress(ipAddress: String): SubnetDevices {
            require(isIPv4Address(ipAddress)) { "Invalid IP Address" }
            val segment = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1)
            val subnetDevice = SubnetDevices()
            subnetDevice.addresses = ArrayList()

            // Get addresses from ARP Info first as they are likely to be reachable
            for (ip in allIPAddressesInARPCache) {
                if (ip.startsWith(segment)) {
                    subnetDevice.addresses!!.add(ip)
                }
            }

            // Add all missing addresses in subnet
            for (j in 0..254) {
                if (!subnetDevice.addresses!!.contains(segment + j)) {
                    subnetDevice.addresses!!.add(segment + j)
                }
            }
            return subnetDevice
        }

        /**
         * @param ipAddresses - the ipAddresses of devices to be checked
         *
         * @return - this for chaining
         */
        fun fromIPList(ipAddresses: List<String>?): SubnetDevices {
            val subnetDevice = SubnetDevices()
            subnetDevice.addresses = ArrayList()
            subnetDevice.addresses!!.addAll(ipAddresses!!)
            return subnetDevice
        }
    }
}