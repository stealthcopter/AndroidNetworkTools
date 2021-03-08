package com.stealthcopter.networktools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * Tested this and it wakes my computer up :)
 *
 * Ref: http://www.jibble.org/wake-on-lan/
 */
public class WakeOnLan {

    public static final int DEFAULT_PORT = 9;
    public static final int DEFAULT_TIMEOUT_MILLIS = 10000;
    public static final int DEFAULT_NO_PACKETS = 5;

    private String ipStr;
    private InetAddress inetAddress;
    private String macStr;
    private int port = DEFAULT_PORT;
    private int timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
    private int noPackets = DEFAULT_NO_PACKETS;

    public interface WakeOnLanListener {
        void onSuccess();

        void onError(Exception e);
    }

    // This class is not to be instantiated
    private WakeOnLan() {
    }

    /**
     * Set the ip address to wake
     *
     * @param ipStr - IP Address to be woke
     * @return this object to allow chaining
     */
    public static WakeOnLan onIp(String ipStr) {
        WakeOnLan wakeOnLan = new WakeOnLan();
        wakeOnLan.ipStr = ipStr;
        return wakeOnLan;
    }


    /**
     * Set the address to wake
     *
     * @param inetAddress - InetAddress to be woke
     * @return this object to allow chaining
     */
    public static WakeOnLan onAddress(InetAddress inetAddress) {
        WakeOnLan wakeOnLan = new WakeOnLan();
        wakeOnLan.inetAddress = inetAddress;
        return wakeOnLan;
    }


    /**
     * Set the mac address of the device to wake
     *
     * @param macStr - The MAC address of the device to be woken (Required)
     * @return this object to allow chaining
     */
    public WakeOnLan withMACAddress(String macStr) {
        if (macStr == null) throw new NullPointerException("MAC Cannot be null");
        this.macStr = macStr;
        return this;
    }


    /**
     * Sets the port to send the packet to, default is 9
     *
     * @param port - the port for the wol packet
     * @return this object to allow chaining
     */
    public WakeOnLan setPort(int port) {
        if (port <= 0 || port > 65535) throw new IllegalArgumentException("Invalid port " + port);
        this.port = port;
        return this;
    }


    /**
     * Sets the number of packets to send, this is to overcome the flakiness of networks
     *
     * @param noPackets - the numbe of packets to send
     * @return this object to allow chaining
     */
    public WakeOnLan setNoPackets(int noPackets) {
        if (noPackets <= 0)
            throw new IllegalArgumentException("Invalid number of packets to send " + noPackets);
        this.noPackets = noPackets;
        return this;
    }

    /**
     * Sets the number milliseconds for the timeout on the socket send
     *
     * @param timeoutMillis - the timeout in milliseconds
     * @return this object to allow chaining
     */
    public WakeOnLan setTimeout(int timeoutMillis) {
        if (timeoutMillis <= 0)
            throw new IllegalArgumentException("Timeout cannot be less than zero");
        this.timeoutMillis = timeoutMillis;
        return this;
    }


    /**
     * Synchronous call of the wake method. Note that this is a network request and should not be
     * performed on the UI thread
     *
     * @throws IOException - Thrown from socket errors
     */
    public void wake() throws IOException {

        if (ipStr == null && inetAddress == null) {
            throw new IllegalArgumentException("You must declare ip address or supply an inetaddress");
        }

        if (macStr == null) {
            throw new NullPointerException("You did not supply a mac address with withMac(...)");
        }

        if (ipStr != null) {
            sendWakeOnLan(ipStr, macStr, port, timeoutMillis, noPackets);
        } else {
            sendWakeOnLan(inetAddress, macStr, port, timeoutMillis, noPackets);
        }
    }


    /**
     * Asynchronous call of the wake method. This will be performed on the background thread
     * and optionally fire a listener when complete, or when an error occurs
     *
     * @param wakeOnLanListener - listener to call on result
     */
    public void wake(final WakeOnLanListener wakeOnLanListener) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wake();
                    if (wakeOnLanListener != null) wakeOnLanListener.onSuccess();
                } catch (IOException e) {
                    if (wakeOnLanListener != null) wakeOnLanListener.onError(e);
                }

            }
        });

        thread.start();
    }

    /**
     * Send a Wake-On-Lan packet to port 9 using default timeout of 10s
     *
     * @param ipStr  - IP String to send to
     * @param macStr - MAC address to wake up
     *
     * @throws IllegalArgumentException - invalid ip or mac
     * @throws IOException - error sending packet
     */
    public static void sendWakeOnLan(String ipStr, String macStr) throws IllegalArgumentException, IOException {
        sendWakeOnLan(ipStr, macStr, DEFAULT_PORT, DEFAULT_TIMEOUT_MILLIS, DEFAULT_NO_PACKETS);
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
    public static void sendWakeOnLan(final String ipStr, final String macStr, final int port, final int timeoutMillis, final int packets) throws IllegalArgumentException, IOException {
        if (ipStr == null) throw new IllegalArgumentException("Address cannot be null");
        InetAddress address = InetAddress.getByName(ipStr);
        sendWakeOnLan(address, macStr, port, timeoutMillis, packets);
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
    public static void sendWakeOnLan(final InetAddress address, final String macStr, final int port, final int timeoutMillis, final int packets) throws IllegalArgumentException, IOException {
        if (address == null) throw new IllegalArgumentException("Address cannot be null");
        if (macStr == null) throw new IllegalArgumentException("MAC Address cannot be null");
        if (port <= 0 || port > 65535) throw new IllegalArgumentException("Invalid port " + port);
        if (packets <= 0)
            throw new IllegalArgumentException("Invalid number of packets to send " + packets);

        byte[] macBytes = MACTools.getMacBytes(macStr);
        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

        // Wake on lan is unreliable so best to send the packet a few times
        for (int i = 0; i < packets; i++) {
            DatagramSocket socket = new DatagramSocket();

            socket.setSoTimeout(timeoutMillis);

            socket.send(packet);
            socket.close();
        }
    }


}