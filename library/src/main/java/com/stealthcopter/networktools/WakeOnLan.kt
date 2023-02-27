package com.stealthcopter.networktools

import com.stealthcopter.networktools.MACTools.getMacBytes
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 *
 * Tested this and it wakes my computer up :)
 *
 * Ref: http://www.jibble.org/wake-on-lan/
 */
class WakeOnLan  // This class is not to be instantiated
private constructor() {
    private var ipStr: String? = null
    private var inetAddress: InetAddress? = null
    private var macStr: String? = null
    private var port = DEFAULT_PORT
    private var timeoutMillis = DEFAULT_TIMEOUT_MILLIS
    private var noPackets = DEFAULT_NO_PACKETS

    interface WakeOnLanListener {
        fun onSuccess()
        fun onError(e: Exception?)
    }

    /**
     * Set the mac address of the device to wake
     *
     * @param macStr - The MAC address of the device to be woken (Required)
     * @return this object to allow chaining
     */
    fun withMACAddress(macStr: String?): WakeOnLan {
        if (macStr == null) throw NullPointerException("MAC Cannot be null")
        this.macStr = macStr
        return this
    }

    /**
     * Sets the port to send the packet to, default is 9
     *
     * @param port - the port for the wol packet
     * @return this object to allow chaining
     */
    fun setPort(port: Int): WakeOnLan {
        require(!(port <= 0 || port > 65535)) { "Invalid port $port" }
        this.port = port
        return this
    }

    /**
     * Sets the number of packets to send, this is to overcome the flakiness of networks
     *
     * @param noPackets - the numbe of packets to send
     * @return this object to allow chaining
     */
    fun setNoPackets(noPackets: Int): WakeOnLan {
        require(noPackets > 0) { "Invalid number of packets to send $noPackets" }
        this.noPackets = noPackets
        return this
    }

    /**
     * Sets the number milliseconds for the timeout on the socket send
     *
     * @param timeoutMillis - the timeout in milliseconds
     * @return this object to allow chaining
     */
    fun setTimeout(timeoutMillis: Int): WakeOnLan {
        require(timeoutMillis > 0) { "Timeout cannot be less than zero" }
        this.timeoutMillis = timeoutMillis
        return this
    }

    /**
     * Synchronous call of the wake method. Note that this is a network request and should not be
     * performed on the UI thread
     *
     * @throws IOException - Thrown from socket errors
     */
    @Throws(IOException::class)
    fun wake() {
        require(!(ipStr == null && inetAddress == null)) { "You must declare ip address or supply an inetaddress" }
        if (macStr == null) {
            throw NullPointerException("You did not supply a mac address with withMac(...)")
        }
        if (ipStr != null) {
            sendWakeOnLan(ipStr, macStr, port, timeoutMillis, noPackets)
        } else {
            sendWakeOnLan(inetAddress, macStr, port, timeoutMillis, noPackets)
        }
    }

    /**
     * Asynchronous call of the wake method. This will be performed on the background thread
     * and optionally fire a listener when complete, or when an error occurs
     *
     * @param wakeOnLanListener - listener to call on result
     */
    fun wake(wakeOnLanListener: WakeOnLanListener?) {
        val thread = Thread {
            try {
                wake()
                wakeOnLanListener?.onSuccess()
            } catch (e: IOException) {
                wakeOnLanListener?.onError(e)
            }
        }
        thread.start()
    }

    companion object {
        const val DEFAULT_PORT = 9
        const val DEFAULT_TIMEOUT_MILLIS = 10000
        const val DEFAULT_NO_PACKETS = 5

        /**
         * Set the ip address to wake
         *
         * @param ipStr - IP Address to be woke
         * @return this object to allow chaining
         */
        fun onIp(ipStr: String?): WakeOnLan {
            val wakeOnLan = WakeOnLan()
            wakeOnLan.ipStr = ipStr
            return wakeOnLan
        }

        /**
         * Set the address to wake
         *
         * @param inetAddress - InetAddress to be woke
         * @return this object to allow chaining
         */
        fun onAddress(inetAddress: InetAddress?): WakeOnLan {
            val wakeOnLan = WakeOnLan()
            wakeOnLan.inetAddress = inetAddress
            return wakeOnLan
        }
        /**
         * Send a Wake-On-Lan packet
         *
         * @param ipStr         - IP String to send wol packet to
         * @param macStr        - MAC address to wake up
         * @param port          - port to send packet to
         * @param timeoutMillis - timeout (millis)
         * @param packets       - number of packets to send
         *
         * @throws IllegalArgumentException - invalid ip or mac
         * @throws IOException - error sending packet
         */
        /**
         * Send a Wake-On-Lan packet to port 9 using default timeout of 10s
         *
         * @param ipStr  - IP String to send to
         * @param macStr - MAC address to wake up
         *
         * @throws IllegalArgumentException - invalid ip or mac
         * @throws IOException - error sending packet
         */
        @JvmOverloads
        @Throws(IllegalArgumentException::class, IOException::class)
        fun sendWakeOnLan(
            ipStr: String?,
            macStr: String?,
            port: Int = DEFAULT_PORT,
            timeoutMillis: Int = DEFAULT_TIMEOUT_MILLIS,
            packets: Int = DEFAULT_NO_PACKETS
        ) {
            requireNotNull(ipStr) { "Address cannot be null" }
            val address = InetAddress.getByName(ipStr)
            sendWakeOnLan(address, macStr, port, timeoutMillis, packets)
        }

        /**
         * Send a Wake-On-Lan packet
         *
         * @param address       - InetAddress to send wol packet to
         * @param macStr        - MAC address to wake up
         * @param port          - port to send packet to
         * @param timeoutMillis - timeout (millis)
         * @param packets       - number of packets to send
         *
         * @throws IllegalArgumentException - invalid ip or mac
         * @throws IOException - error sending packet
         */
        @Throws(IllegalArgumentException::class, IOException::class)
        fun sendWakeOnLan(
            address: InetAddress?,
            macStr: String?,
            port: Int,
            timeoutMillis: Int,
            packets: Int
        ) {
            requireNotNull(address) { "Address cannot be null" }
            requireNotNull(macStr) { "MAC Address cannot be null" }
            require(!(port <= 0 || port > 65535)) { "Invalid port $port" }
            require(packets > 0) { "Invalid number of packets to send $packets" }
            val macBytes = getMacBytes(macStr)
            val bytes = ByteArray(6 + 16 * macBytes.size)
            for (i in 0..5) {
                bytes[i] = 0xff.toByte()
            }
            run {
                var i = 6
                while (i < bytes.size) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
                    i += macBytes.size
                }
            }
            val packet = DatagramPacket(bytes, bytes.size, address, port)

            // Wake on lan is unreliable so best to send the packet a few times
            for (i in 0 until packets) {
                val socket = DatagramSocket()
                socket.soTimeout = timeoutMillis
                socket.send(packet)
                socket.close()
            }
        }
    }
}