package com.stealthcopter.networktools

import com.stealthcopter.networktools.ping.PingOptions
import com.stealthcopter.networktools.ping.PingResult
import com.stealthcopter.networktools.ping.PingStats
import com.stealthcopter.networktools.ping.PingTools.doPing
import java.net.InetAddress
import java.net.UnknownHostException

class Ping  // This class is not to be instantiated
private constructor() {
    interface PingListener {
        fun onResult(pingResult: PingResult?)
        fun onFinished(pingStats: PingStats?)
        fun onError(e: Exception?)
    }

    private var addressString: String? = null
    private var address: InetAddress? = null
    private val pingOptions = PingOptions()
    private var delayBetweenScansMillis = 0
    private var times = 1
    private var cancelled = false

    /**
     * Set the timeout
     *
     * @param timeOutMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    fun setTimeOutMillis(timeOutMillis: Int): Ping {
        require(timeOutMillis >= 0) { "Times cannot be less than 0" }
        pingOptions.setTimeoutMillis(timeOutMillis)
        return this
    }

    /**
     * Set the delay between each ping
     *
     * @param delayBetweenScansMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    fun setDelayMillis(delayBetweenScansMillis: Int): Ping {
        require(delayBetweenScansMillis >= 0) { "Delay cannot be less than 0" }
        this.delayBetweenScansMillis = delayBetweenScansMillis
        return this
    }

    /**
     * Set the time to live
     *
     * @param timeToLive - the TTL for each ping
     * @return this object to allow chaining
     */
    fun setTimeToLive(timeToLive: Int): Ping {
        require(timeToLive >= 1) { "TTL cannot be less than 1" }
        pingOptions.setTimeToLive(timeToLive)
        return this
    }

    /**
     * Set number of times to ping the address
     *
     * @param noTimes - number of times, 0 = continuous
     * @return this object to allow chaining
     */
    fun setTimes(noTimes: Int): Ping {
        require(noTimes >= 0) { "Times cannot be less than 0" }
        times = noTimes
        return this
    }

    private fun setAddress(address: InetAddress) {
        this.address = address
    }

    /**
     * Set the address string which will be resolved to an address by resolveAddressString()
     *
     * @param addressString - String of the address to be pinged
     */
    private fun setAddressString(addressString: String) {
        this.addressString = addressString
    }

    /**
     * Parses the addressString to an address
     *
     * @throws UnknownHostException - if host cannot be found
     */
    @Throws(UnknownHostException::class)
    private fun resolveAddressString() {
        if (address == null && addressString != null) {
            address = InetAddress.getByName(addressString)
        }
    }

    /**
     * Cancel a running ping
     */
    fun cancel() {
        cancelled = true
    }

    /**
     * Perform a synchronous ping and return a result, will ignore number of times.
     *
     * Note that this should be performed on a background thread as it will perform a network
     * request
     *
     * @return - ping result
     * @throws UnknownHostException - if the host cannot be resolved
     */
    @Throws(UnknownHostException::class)
    fun doPing(): PingResult {
        cancelled = false
        resolveAddressString()
        return doPing(address, pingOptions)
    }

    /**
     * Perform an asynchronous ping
     *
     * @param pingListener - the listener to fire PingResults to.
     * @return - this so we can cancel if needed
     */
    fun doPing(pingListener: PingListener?): Ping {
        Thread(Runnable {
            try {
                resolveAddressString()
            } catch (e: UnknownHostException) {
                pingListener!!.onError(e)
                return@Runnable
            }
            if (address == null) {
                pingListener!!.onError(NullPointerException("Address is null"))
                return@Runnable
            }
            var pingsCompleted: Long = 0
            var noLostPackets: Long = 0
            var totalPingTime = 0f
            var minPingTime = -1f
            var maxPingTime = -1f
            cancelled = false
            var noPings = times

            // times == 0 is the case that we can continuous scanning
            while (noPings > 0 || times == 0) {
                val pingResult = doPing(address, pingOptions)
                pingListener?.onResult(pingResult)

                // Update ping stats
                pingsCompleted++
                if (pingResult.hasError()) {
                    noLostPackets++
                } else {
                    val timeTaken: Float = pingResult.timeTaken
                    totalPingTime += timeTaken
                    if (maxPingTime == -1f || timeTaken > maxPingTime) maxPingTime = timeTaken
                    if (minPingTime == -1f || timeTaken < minPingTime) minPingTime = timeTaken
                }
                noPings--
                if (cancelled) break
                try {
                    Thread.sleep(delayBetweenScansMillis.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            pingListener?.onFinished(
                PingStats(
                    address!!,
                    pingsCompleted,
                    noLostPackets,
                    totalPingTime,
                    minPingTime,
                    maxPingTime
                )
            )
        }).start()
        return this
    }

    companion object {
        // Only try ping using the java method
        const val PING_JAVA = 0

        // Only try ping using the native method (will only work if native ping binary is found)
        const val PING_NATIVE = 1

        // Use a hybrid ping that will attempt to use native binary but fallback to using java method
        // if it's not found.
        const val PING_HYBRID = 2

        /**
         * Set the address to ping
         *
         * Note that a lookup is not performed here so that we do not accidentally perform a network
         * request on the UI thread.
         *
         * @param address - Address to be pinged
         * @return this object to allow chaining
         */
        fun onAddress(address: String): Ping {
            val ping = Ping()
            ping.setAddressString(address)
            return ping
        }

        /**
         * Set the address to ping
         *
         * @param ia - Address to be pinged
         * @return this object to allow chaining
         */
        @JvmStatic
        fun onAddress(ia: InetAddress): Ping {
            val ping = Ping()
            ping.setAddress(ia)
            return ping
        }
    }
}