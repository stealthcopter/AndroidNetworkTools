package com.stealthcopter.networktools.ping;

import java.io.IOException;
import java.net.InetAddress;

public class PingTools {

    // This class is not to be instantiated
    private PingTools() {
    }


    /**
     * Perform a ping using the native ping tool and fall back to using java echo request
     * on failure.
     *
     * @param ia            - address to ping
     * @param pingOptions   - ping command options
     * @return - the ping results
     */
    public static PingResult doPing(InetAddress ia, PingOptions pingOptions) {

        // Try native ping first
        try {
            return PingTools.doNativePing(ia, pingOptions);
        } catch (InterruptedException e) {
            PingResult pingResult = new PingResult(ia);
            pingResult.isReachable = false;
            pingResult.error = "Interrupted";
            return pingResult;
        } catch (Exception ignored) {
        }

        // Fallback to java based ping
        return PingTools.doJavaPing(ia, pingOptions);
    }


    /**
     * Perform a ping using the native ping binary
     *
     * @param ia            - address to ping
     * @param pingOptions   - ping command options
     * @return - the ping results
     * @throws IOException - IO error running ping command
     * @throws InterruptedException - thread interrupt
     */
    public static PingResult doNativePing(InetAddress ia, PingOptions pingOptions) throws IOException, InterruptedException {
        return PingNative.ping(ia, pingOptions);
    }

    /**
     * Tries to reach this {@code InetAddress}. This method first tries to use
     * ICMP <i>(ICMP ECHO REQUEST)</i>, falling back to a TCP connection
     * on port 7 (Echo) of the remote host.
     *
     * @param ia            - address to ping
     * @param pingOptions   - ping command options
     * @return - the ping results
     */
    public static PingResult doJavaPing(InetAddress ia, PingOptions pingOptions) {
        PingResult pingResult = new PingResult(ia);

        if (ia == null) {
            pingResult.isReachable = false;
            return pingResult;
        }

        try {
            long startTime = System.nanoTime();
            final boolean reached = ia.isReachable(null, pingOptions.getTimeToLive(), pingOptions.getTimeoutMillis());
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
