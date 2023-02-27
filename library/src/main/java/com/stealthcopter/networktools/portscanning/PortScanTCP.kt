package com.stealthcopter.networktools.portscanning

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

object PortScanTCP {
    /**
     * Check if a port is open with TCP
     *
     * @param ia            - address to scan
     * @param portNo        - port to scan
     * @param timeoutMillis - timeout
     * @return - true if port is open, false if not or unknown
     */
    fun scanAddress(ia: InetAddress?, portNo: Int, timeoutMillis: Int): Boolean {
        var s: Socket? = null
        try {
            s = Socket()
            s.connect(InetSocketAddress(ia, portNo), timeoutMillis)
            return true
        } catch (e: IOException) {
            // Don't log anything as we are expecting a lot of these from closed ports.
        } finally {
            if (s != null) {
                try {
                    s.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }
}