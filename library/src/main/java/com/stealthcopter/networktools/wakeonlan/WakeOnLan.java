package com.stealthcopter.networktools.wakeonlan;

import android.util.Log;

import com.stealthcopter.networktools.Const;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by mat on 09/12/15.
 */
public class WakeOnLan {

    // http://www.jibble.org/wake-on-lan/

    public static final int PORT = 9;

    public static void sendWakeOnLan(String ipStr, String macStr) throws UnknownHostException, SocketException, IOException {

        byte[] macBytes = getMacBytes(macStr);
        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        InetAddress address = InetAddress.getByName(ipStr);
        String host = address.getHostName();

        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
        DatagramSocket socket = new DatagramSocket();

//        socket.setSoTimeout(1000);

        socket.send(packet);
        socket.close();

        Log.d(Const.TAG, "Wake-on-LAN packet sent.");
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
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
