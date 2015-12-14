package com.stealthcopter.networktools.portscanning;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by mat on 13/12/15.
 */
public class PortScanTCP {

    public static boolean scanAddress(InetAddress ia, int portNo, int timeoutMillis){
        Socket s = null;
        try {
            s = new Socket(ia, portNo);
            s.setSoTimeout(timeoutMillis); // This is pointless as we don't get to this point? unless open???
            return true;
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
        return false;
    }

}
