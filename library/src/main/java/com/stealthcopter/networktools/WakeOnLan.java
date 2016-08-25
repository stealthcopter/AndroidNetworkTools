package com.stealthcopter.networktools;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by mat on 09/12/15.
 *
 * Tested this and it wakes my computer up :)
 *
 * Ref: http://www.jibble.org/wake-on-lan/
 */
public class WakeOnLan {

    public static final int DEFAULT_PORT = 9;
    public static final int DEFAULT_TIMEOUT_MILLIS = 10000;
    public static final int DEFAULT_NO_PACKETS = 5;

    /**
     * Send a Wake-On-Lan packet to port 9 using default timeout of 10s
     * @param ipStr - IP String to send to
     * @param macStr - MAC address to wake up
     */
    public static void sendWakeOnLan(@NonNull String ipStr, @NonNull String macStr) throws IOException, IllegalArgumentException {
        sendWakeOnLan(ipStr, macStr, DEFAULT_PORT, DEFAULT_TIMEOUT_MILLIS, DEFAULT_NO_PACKETS);
    }

    /**
     * Send a Wake-On-Lan packet
     * @param ipStr - IP String to send to
     * @param macStr - MAC address to wake up
     * @param port - port to send packet to
     * @param timeoutMillis - timeout (millis)
     * @param packets - number of packets to send
     */
    public static void sendWakeOnLan(@NonNull String ipStr, @NonNull String macStr, int port, int timeoutMillis, int packets) throws IOException, IllegalArgumentException {

        if (ipStr == null) throw new IllegalArgumentException("Ip Address cannot be null");
        if (macStr == null) throw new IllegalArgumentException("MAC Address cannot be null");
        if (port<=0 || port>65535) throw new IllegalArgumentException("Invalid port "+port);
        if (packets<=0) throw new IllegalArgumentException("Invalid number of packets to send "+packets);

        byte[] macBytes = getMacBytes(macStr);
        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        InetAddress address = InetAddress.getByName(ipStr);

        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

        // Wake on lan is unreliable so best to send the packet a few times
        for (int i = 0; i < packets; i++) {
            DatagramSocket socket = new DatagramSocket();

            socket.setSoTimeout(timeoutMillis);

            socket.send(packet);
            socket.close();
        }
    }

        /**
         * Convert a MAC string to bytes
         * @param macStr - MAC string
         * @return - MAC formatted in bytes
         * @throws IllegalArgumentException
         */
    private static byte[] getMacBytes(@NonNull String macStr) throws IllegalArgumentException {

        if (macStr==null) throw new IllegalArgumentException("Mac Address cannot be null");

        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

}
