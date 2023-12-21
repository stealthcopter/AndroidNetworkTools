package com.stealthcopter.networktools.ping

import com.stealthcopter.networktools.ping.PingNative.ping
import java.io.IOException
import java.net.InetAddress

object PingTools {
    /**
     * Perform a ping using the native ping tool and fall back to using java echo request
     * on failure.
     *
     * @param ia            - address to ping
     * @param pingOptions   - ping command options
     * @return - the ping results
     */
    @JvmStatic
    fun doPing(ia: InetAddress?, pingOptions: PingOptions): PingResult {

        // Try native ping first
        try {
            return doNativePing(ia, pingOptions)
        } catch (e: InterruptedException) {
            val pingResult = PingResult(ia!!)
            pingResult.isReachable = false
            pingResult.error = "Interrupted"
            return pingResult
        } catch (ignored: Exception) {
        }

        // Fallback to java based ping
        return doJavaPing(ia, pingOptions)
    }

    /**
     * Perform a ping using the native ping binary
     *
     * @param ia            - address to ping
     * @param pingOptions   - ping command options
     * @return - the ping results
     * @throws IOException - IO error running ping command
     * @throws InterruptedException - thread interrupt
     */
    @Throws(IOException::class, InterruptedException::class)
    fun doNativePing(ia: InetAddress?, pingOptions: PingOptions?): PingResult {
        return ping(ia, pingOptions!!)
    }

    /**
     * Tries to reach this `InetAddress`. This method first tries to use
     * ICMP *(ICMP ECHO REQUEST)*, falling back to a TCP connection
     * on port 7 (Echo) of the remote host.
     *
     * @param ia            - address to ping
     * @param pingOptions   - ping command options
     * @return - the ping results
     */
    fun doJavaPing(ia: InetAddress?, pingOptions: PingOptions): PingResult {
        val pingResult = PingResult(ia!!)
        if (ia == null) {
            pingResult.isReachable = false
            return pingResult
        }
        try {
            val startTime = System.nanoTime()
            val reached =
                ia.isReachable(null, pingOptions.getTimeToLive(), pingOptions.getTimeoutMillis())
            pingResult.timeTaken = (System.nanoTime() - startTime) / 1e6f
            pingResult.isReachable = reached
            if (!reached) pingResult.error = "Timed Out"
        } catch (e: IOException) {
            pingResult.isReachable = false
            pingResult.error = "IOException: " + e.message
        }
        return pingResult
    }
}