package com.stealthcopter.networktools.ping;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by mat on 09/12/15.
 */
public class PingTools {

    /**
     * This will perform a ping using the native ping tool and fall back to using a java style
     * request on failure.
     */
    public static PingResult doPing(InetAddress ia, int timeOutMillis){

        // Try native ping first
        try{
            PingResult result = PingTools.doNativePing(ia, timeOutMillis);
            return result;
        } catch (InterruptedException e){
            PingResult pingResult = new PingResult(ia);
            pingResult.isReachable = false;
            pingResult.error="Interrupted";
            return pingResult;
        }
        catch (Exception ignored){
        }

        Log.v("AndroidNetworkTools", "Native ping failed, using java");

        // Fallback to java based ping
        return PingTools.doJavaPing(ia, timeOutMillis);
    }


    public static PingResult doNativePing(InetAddress ia, int timeOutMillis) throws IOException, InterruptedException {
        return PingNative.ping(ia, timeOutMillis);
    }

    /**
     * Tries to reach this {@code InetAddress}. This method first tries to use
     * ICMP <i>(ICMP ECHO REQUEST)</i>, falling back to a TCP connection
     * on port 7 (Echo) of the remote host.
     */
    public static PingResult doJavaPing(InetAddress ia, int timeOutMillis){
        PingResult pingResult = new PingResult(ia);
        try {
            long startTime = System.nanoTime();
            final boolean reached = ia.isReachable(timeOutMillis);
            pingResult.timeTaken = (System.nanoTime()-startTime)/1e6f;
            pingResult.isReachable = reached;
            if (!reached) pingResult.error="Timed Out";
        } catch (IOException e) {
            pingResult.isReachable=false;
            pingResult.error="IOException: "+e.getMessage();
        }
        return pingResult;
    }

}
