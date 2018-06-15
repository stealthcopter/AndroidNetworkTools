package com.stealthcopter.networktools.traceroute;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Traceroute class
 *
 * Idea: Use nmap if available..
 */
public class Traceroute {

    private int maxHops = 30;
    private int timeOutMillis = 1000;
    private InetAddress address;
    private boolean shouldStopTrace = false;
    private ArrayList<TraceObj> traceObjs;
    private OnTraceListener onTraceListener;

    public interface OnTraceListener {
        void onTrace(TraceObj traceObj);
        void onFinishTrace(List<TraceObj> traceObjs);
        void onError(String error);
    }

    // This class is not to be instantiated
    private Traceroute() {

    }

    /**
     * Set the address to traceroute
     *
     * @param address - Address to be traceroute
     * @return this object to allow chaining
     * @throws UnknownHostException - if no IP address for the
     *                              <code>host</code> could be found, or if a scope_id was specified
     *                              for a global IPv6 address.
     */
    public static Traceroute onAddress(String address) throws UnknownHostException {
        return onAddress(InetAddress.getByName(address));
    }

    /**
     * Set the address to traceroute
     *
     * @param ia - Address to be traced
     * @return this object to allow chaining
     */
    public static Traceroute onAddress(InetAddress ia) {
        Traceroute traceroute = new Traceroute();
        traceroute.address = ia;
        return traceroute;
    }

    /**
     * Cancel a running traceroute
     */
    public void cancel() {
        shouldStopTrace = true;
    }

    /**
     * Sets the timeout for each leg of traceroute. Note that because we are using the ping command
     * this will be converted into seconds, so minimum of 1000ms and increasing in increments of 1000
     *
     * @param timeOutMillis - the timeout for each ping in milliseconds
     *                      Recommendations: 3
     * @return this object to allow chaining
     */
    public Traceroute setTimeOutMillis(int timeOutMillis) {
        if (timeOutMillis < 0) throw new IllegalArgumentException("Timeout cannot be less than 0");
        this.timeOutMillis = Math.min(1000, timeOutMillis);
        return this;
    }

    /**
     * Sets the maximum number of hops allowed
     *
     * @param hops - the maximum number of hops
     *
     * @return this object to allow chaining
     */
    public Traceroute setMaxHops(int hops) {
        if (hops < 1) throw new IllegalArgumentException("Hops cannot be less than 1");
        this.maxHops = hops;
        return this;
    }

    /**
     * @param onTraceListener - the listener to fire portscan results to.
     * @return - this object so we can cancel the scan if needed
     */
    public void traceRoute(final OnTraceListener onTraceListener) {

        this.onTraceListener = onTraceListener;

        // If


        // TODO: Chunk in a thread

        traceObjs = new ArrayList<>();

        int hops = 0;

        try {

            for (int i = 1; i <= maxHops; i++) {

                if (shouldStopTrace) {
                    shouldStopTrace = false;
                    finishTrace();
                    return;
                }

                StringBuilder echo = new StringBuilder();
                Runtime runtime = Runtime.getRuntime();

                // Ping command takes seconds, so convert
                int traceTimeoutSeconds = timeOutMillis / 1000;

                String command = "ping -c 4 -t " + i + " -w " + traceTimeoutSeconds + " -W " + traceTimeoutSeconds + " " + address.getHostName();

                Process proc = runtime.exec(command);
                proc.waitFor();
                int exit = proc.exitValue();
                exit = 1;
                if (exit < 2) {
                    InputStreamReader reader = new InputStreamReader(proc.getInputStream());
                    BufferedReader buffer = new BufferedReader(reader);
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        echo.append(line);
                    }

                    String str = echo.toString();

                    if (str.contains("Time to live exceeded")) {

                        TraceObj traceObj = new TraceObj();
                        traceObj.fullString = str;
                        traceObj.address = str.substring(str.indexOf("data.From ") + "data.From ".length(), str.indexOf("icmp_seq") - 1).replace(":", "");
                        if (traceObj.address.contains(" (")) {
                            traceObj.hostname = traceObj.address.substring(0, traceObj.address.indexOf(" ("));
                            traceObj.ip = traceObj.address.substring(traceObj.address.indexOf(" (") + 2, traceObj.address.length() - 1);
                        } else {
                            traceObj.ip = traceObj.address;
                        }
                        traceObj.hop = hops++;

                        addTraceObj(traceObj);

                        if (shouldStopTrace) {
                            // Stop before possibly doing activity_ping.
                            shouldStopTrace = false;
                            return;
                        }
                    }
                    else if (!str.contains("100% packet loss")) {
                        // Keep going if it's a route, or if we lost all our packets

                        TraceObj traceObj = new TraceObj();

                        traceObj.address = str.substring(str.indexOf("bytes of data.64 bytes from ") + "bytes of data.64 bytes from ".length(), str.indexOf("icmp_seq") - 1).replace(":", "");
                        if (traceObj.address.contains(" (")) {
                            traceObj.hostname = traceObj.address.substring(0, traceObj.address.indexOf(" ("));
                            traceObj.ip = traceObj.address.substring(traceObj.address.indexOf(" (") + 2, traceObj.address.length() - 1);
                        } else {
                            traceObj.ip = traceObj.address;
                        }
                        traceObj.fullString = str;
                        traceObj.hop = hops++;
                        traceObj.isTargetDestination = true;
                        traceObj.time = str.substring(str.indexOf("time="), str.indexOf("ms", str.indexOf("time=")));

                        addTraceObj(traceObj);
                        finishTrace();
                        return;
                    }

                } else {
                    onTraceListener.onError("Native ping command failed");
                }
            }

        } catch (Exception e) {
            onTraceListener.onError("Native ping command failed");
        }
    }

    private void addTraceObj(TraceObj traceObj){
        traceObjs.add(traceObj);
        onTraceListener.onTrace(traceObj);
    }
    private void finishTrace(){
        onTraceListener.onFinishTrace(traceObjs);
    }

}
