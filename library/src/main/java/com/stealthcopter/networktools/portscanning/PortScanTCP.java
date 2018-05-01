package com.stealthcopter.networktools.portscanning;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by mat on 13/12/15.
 */
public class PortScanTCP {

    // This class is not to be instantiated
    private PortScanTCP() {
    }

    public static PortInfo scanAddress(InetAddress ia, int portNo, int timeoutMillis){

        PortInfo portInfo = new PortInfo(ia.getHostAddress(), portNo);
        portInfo.open = false;

        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(ia, portNo), timeoutMillis);

            portInfo.open = true;
        } catch (IOException e) {
            // Don't log anything as we are expecting a lot of these from closed ports.
        }
        finally {
            if (s!=null){
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return portInfo;
    }

}
