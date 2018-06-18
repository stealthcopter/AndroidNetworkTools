package com.stealthcopter.networktools.ping;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by mat on 09/12/15.
 */
public class PingTools {

    // This class is not to be instantiated
    private PingTools() {
    }


    /**
     * Perform a ping using the native ping tool and fall back to using java echo request
     * on failure.
     *
     * @param ia            - address to ping
     * @param timeOutMillis - timeout in millisecdonds
     * @return - the ping results
     */
    public static PingResult doPing(InetAddress ia, int timeOutMillis) {

        // Try native ping first
        try {
            return PingTools.doNativePing(ia, timeOutMillis);
        } catch (InterruptedException e) {
            PingResult pingResult = new PingResult(ia);
            pingResult.isReachable = false;
            pingResult.error = "Interrupted";
            return pingResult;
        } catch (Exception ignored) {
        }

        // Fallback to java based ping
        return PingTools.doJavaPing(ia, timeOutMillis);
    }


    /**
     * Perform a ping using the native ping binary
     *
     * @param ia            - address to ping
     * @param timeOutMillis - timeout in millisecdonds
     * @return - the ping results
     * @throws IOException - IO error running ping command
     * @throws InterruptedException - thread interupt
     */
    public static PingResult doNativePing(InetAddress ia, int timeOutMillis) throws IOException, InterruptedException {
        return PingNative.ping(ia, timeOutMillis);
    }

    /**
     * Tries to reach this {@code InetAddress}. This method first tries to use
     * ICMP <i>(ICMP ECHO REQUEST)</i>, falling back to a TCP connection
     * on port 7 (Echo) of the remote host.
     *
     * @param ia            - address to ping
     * @param timeOutMillis - timeout in millisecdonds
     * @return - the ping results
     */
    public static PingResult doJavaPing(InetAddress ia, int timeOutMillis) {
        PingResult pingResult = new PingResult(ia);
        try {
            long startTime = System.nanoTime();
            final boolean reached = ia.isReachable(timeOutMillis);
            pingResult.timeTaken = (System.nanoTime() - startTime) / 1e6f;
            pingResult.isReachable = reached;
            if (!reached) pingResult.error = "Timed Out";
        } catch (IOException e) {
            pingResult.isReachable = false;
            pingResult.error = "IOException: " + e.getMessage();
        }
        return pingResult;
    }

}
