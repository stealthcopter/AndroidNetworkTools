package com.stealthcopter.networktools

import com.stealthcopter.networktools.IPTools.isIpAddressLocalNetwork
import com.stealthcopter.networktools.IPTools.isIpAddressLocalhost
import com.stealthcopter.networktools.portscanning.PortScanTCP
import com.stealthcopter.networktools.portscanning.PortScanUDP
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PortScan  // This class is not to be instantiated
private constructor() {
    private var method = METHOD_TCP
    private var noThreads = 50
    private var address: InetAddress? = null
    private var timeOutMillis = 1000
    private var cancelled = false
    private var ports = ArrayList<Int>()
    private val openPortsFound = ArrayList<Int>()
    private var portListener: PortListener? = null

    interface PortListener {
        fun onResult(portNo: Int, open: Boolean)
        fun onFinished(openPorts: ArrayList<Int>?)
    }

    /**
     * Sets the timeout for each port scanned
     *
     *
     * If you raise the timeout you may want to consider increasing the thread count [.setNoThreads] to compensate.
     * We can afford to have quite a high thread count as most of the time the thread is just sitting
     * idle and waiting for the socket to timeout.
     *
     * @param timeOutMillis - the timeout for each ping in milliseconds
     * Recommendations:
     * Local host: 20 - 500 ms - can be very fast as request doesn't need to go over network
     * Local network 500 - 2500 ms
     * Remote Scan 2500+ ms
     * @return this object to allow chaining
     */
    fun setTimeOutMillis(timeOutMillis: Int): PortScan {
        require(timeOutMillis >= 0) { "Timeout cannot be less than 0" }
        this.timeOutMillis = timeOutMillis
        return this
    }

    /**
     * Scan the ports to scan
     *
     * @param port - the port to scan
     * @return this object to allow chaining
     */
    fun setPort(port: Int): PortScan {
        ports.clear()
        validatePort(port)
        ports.add(port)
        return this
    }

    /**
     * Scan the ports to scan
     *
     * @param ports - the ports to scan
     * @return this object to allow chaining
     */
    fun setPorts(ports: ArrayList<Int>): PortScan {

        // Check all ports are valid
        for (port in ports) {
            validatePort(port)
        }
        this.ports = ports
        return this
    }

    /**
     * Scan the ports to scan
     *
     * @param portString - the ports to scan (comma separated, hyphen denotes a range). For example:
     * "21-23,25,45,53,80"
     * @return this object to allow chaining
     */
    fun setPorts(portString: String?): PortScan {
        var portString = portString
        ports.clear()
        val ports = ArrayList<Int>()
        requireNotNull(portString) { "Empty port string not allowed" }
        portString = portString.substring(portString.indexOf(":") + 1, portString.length)
        for (x in portString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (x.contains("-")) {
                val start = x.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0].toInt()
                val end =
                    x.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
                validatePort(start)
                validatePort(end)
                require(end > start) { "Start port cannot be greater than or equal to the end port" }
                for (j in start..end) {
                    ports.add(j)
                }
            } else {
                val start = x.toInt()
                validatePort(start)
                ports.add(start)
            }
        }
        this.ports = ports
        return this
    }

    /**
     * Checks and throws exception if port is not valid
     *
     * @param port - the port to validate
     */
    private fun validatePort(port: Int) {
        require(port >= 1) { "Start port cannot be less than 1" }
        require(port <= 65535) { "Start cannot be greater than 65535" }
    }

    /**
     * Scan all privileged ports
     *
     * @return this object to allow chaining
     */
    fun setPortsPrivileged(): PortScan {
        ports.clear()
        for (i in 1..1023) {
            ports.add(i)
        }
        return this
    }

    /**
     * Scan all ports
     *
     * @return this object to allow chaining
     */
    fun setPortsAll(): PortScan {
        ports.clear()
        for (i in 1..65535) {
            ports.add(i)
        }
        return this
    }

    private fun setAddress(address: InetAddress) {
        this.address = address
    }

    private fun setDefaultThreadsAndTimeouts() {
        // Try and work out automatically what kind of host we are scanning
        // local host (this device) / local network / remote
        if (isIpAddressLocalhost(address)) {
            // If we are scanning a the localhost set the timeout to be very short so we get faster results
            // This will be overridden if user calls setTimeoutMillis manually.
            timeOutMillis = TIMEOUT_LOCALHOST
            noThreads = DEFAULT_THREADS_LOCALHOST
        } else if (isIpAddressLocalNetwork(address)) {
            // Assume local network (not infallible)
            timeOutMillis = TIMEOUT_LOCALNETWORK
            noThreads = DEFAULT_THREADS_LOCALNETWORK
        } else {
            // Assume remote network timeouts
            timeOutMillis = TIMEOUT_REMOTE
            noThreads = DEFAULT_THREADS_REMOTE
        }
    }

    /**
     * @param noThreads set the number of threads to work with, note we default to a large number
     * as these requests are network heavy not cpu heavy.
     * @return self
     * @throws IllegalArgumentException - if no threads is less than 1
     */
    @Throws(IllegalArgumentException::class)
    fun setNoThreads(noThreads: Int): PortScan {
        require(noThreads >= 1) { "Cannot have less than 1 thread" }
        this.noThreads = noThreads
        return this
    }

    /**
     * Set scan method, either TCP or UDP
     *
     * @param method - the transport method to use to scan, either PortScan.METHOD_UDP or PortScan.METHOD_TCP
     * @return this object to allow chaining
     * @throws IllegalArgumentException - if invalid method
     */
    private fun setMethod(method: Int): PortScan {
        when (method) {
            METHOD_UDP, METHOD_TCP -> this.method = method
            else -> throw IllegalArgumentException("Invalid method type $method")
        }
        return this
    }

    /**
     * Set scan method to UDP
     *
     * @return this object to allow chaining
     */
    fun setMethodUDP(): PortScan {
        setMethod(METHOD_UDP)
        return this
    }

    /**
     * Set scan method to TCP
     *
     * @return this object to allow chaining
     */
    fun setMethodTCP(): PortScan {
        setMethod(METHOD_TCP)
        return this
    }

    /**
     * Cancel a running ping
     */
    fun cancel() {
        cancelled = true
    }

    /**
     * Perform a synchronous (blocking) port scan and return a list of open ports
     *
     * @return - ping result
     */
    fun doScan(): ArrayList<Int> {
        cancelled = false
        openPortsFound.clear()
        val executor = Executors.newFixedThreadPool(noThreads)
        for (portNo in ports) {
            val worker: Runnable = PortScanRunnable(address, portNo, timeOutMillis, method)
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
        Collections.sort(openPortsFound)
        return openPortsFound
    }

    /**
     * Perform an asynchronous (non-blocking) port scan
     *
     * @param portListener - the listener to fire portscan results to.
     * @return - this object so we can cancel the scan if needed
     */
    fun doScan(portListener: PortListener?): PortScan {
        this.portListener = portListener
        openPortsFound.clear()
        cancelled = false
        Thread {
            val executor = Executors.newFixedThreadPool(noThreads)
            for (portNo in ports) {
                val worker: Runnable = PortScanRunnable(address, portNo, timeOutMillis, method)
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
            if (portListener != null) {
                Collections.sort(openPortsFound)
                portListener.onFinished(openPortsFound)
            }
        }.start()
        return this
    }

    @Synchronized
    private fun portScanned(port: Int, open: Boolean) {
        if (open) {
            openPortsFound.add(port)
        }
        if (portListener != null) {
            portListener!!.onResult(port, open)
        }
    }

    private inner class PortScanRunnable internal constructor(
        private val address: InetAddress?,
        private val portNo: Int,
        private val timeOutMillis: Int,
        private val method: Int
    ) : Runnable {
        override fun run() {
            if (cancelled) return
            when (method) {
                METHOD_UDP -> portScanned(
                    portNo,
                    PortScanUDP.scanAddress(address, portNo, timeOutMillis)
                )
                METHOD_TCP -> portScanned(
                    portNo,
                    PortScanTCP.scanAddress(address, portNo, timeOutMillis)
                )
                else -> throw IllegalArgumentException("Invalid method")
            }
        }
    }

    companion object {
        private const val TIMEOUT_LOCALHOST = 25
        private const val TIMEOUT_LOCALNETWORK = 1000
        private const val TIMEOUT_REMOTE = 2500
        private const val DEFAULT_THREADS_LOCALHOST = 7
        private const val DEFAULT_THREADS_LOCALNETWORK = 50
        private const val DEFAULT_THREADS_REMOTE = 50
        private const val METHOD_TCP = 0
        private const val METHOD_UDP = 1

        /**
         * Set the address to ping
         *
         * @param address - Address to be pinged
         * @return this object to allow chaining
         * @throws UnknownHostException - if no IP address for the
         * `host` could be found, or if a scope_id was specified
         * for a global IPv6 address.
         */
        @Throws(UnknownHostException::class)
        fun onAddress(address: String?): PortScan {
            return onAddress(InetAddress.getByName(address))
        }

        /**
         * Set the address to ping
         *
         * @param ia - Address to be pinged
         * @return this object to allow chaining
         */
        fun onAddress(ia: InetAddress): PortScan {
            val portScan = PortScan()
            portScan.setAddress(ia)
            portScan.setDefaultThreadsAndTimeouts()
            return portScan
        }
    }
}