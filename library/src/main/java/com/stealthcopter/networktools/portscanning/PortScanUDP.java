package com.stealthcopter.networktools.portscanning;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;

/**
 * Created by mat on 13/12/15.
 */
public class PortScanUDP {

    // This class is not to be instantiated
    private PortScanUDP() {
    }

    public static PortInfo scanAddress(InetAddress ia, int portNo, int timeoutMillis){

        PortInfo portInfo = new PortInfo(ia.getHostAddress(), portNo);
        portInfo.open = false;

        try {
            byte[] bytes = new byte[128];
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length);

            DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(timeoutMillis);
            ds.connect(ia, portNo);
            ds.send(dp);
            ds.isConnected();
            ds.receive(dp);
            ds.close();
        } catch (PortUnreachableException e) {
            portInfo.openState = "closed";
        } catch (SocketTimeoutException e) {
            portInfo.open = true;
            portInfo.openState = "open|filtered";
        } catch (IOException e) {
            portInfo.openState = "unknown";
        }
        return portInfo;
    }

}
