package com.stealthcopter.networktools;

import com.stealthcopter.networktools.ping.PingOptions;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.stealthcopter.networktools.ping.PingTools;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ping {

    // Only try ping using the java method
    public static final int PING_JAVA = 0;

    // Only try ping using the native method (will only work if native ping binary is found)
    public static final int PING_NATIVE = 1;

    // Use a hybrid ping that will attempt to use native binary but fallback to using java method
    // if it's not found.
    public static final int PING_HYBRID = 2;

    // This class is not to be instantiated
    private Ping() {
    }

    public interface PingListener {
        void onResult(PingResult pingResult);
        void onFinished(PingStats pingStats);
        void onError(Exception e);
    }

    private String addressString = null;
    private InetAddress address;
    private final PingOptions pingOptions = new PingOptions();
    private int delayBetweenScansMillis = 0;
    private int times = 1;
    private boolean cancelled = false;

    /**
     * Set the address to ping
     *
     * Note that a lookup is not performed here so that we do not accidentally perform a network
     * request on the UI thread.
     *
     * @param address - Address to be pinged
     * @return this object to allow chaining
     */
    public static Ping onAddress(String address) {
        Ping ping = new Ping();
        ping.setAddressString(address);
        return ping;
    }

    /**
     * Set the address to ping
     *
     * @param ia - Address to be pinged
     * @return this object to allow chaining
     */
    public static Ping onAddress(InetAddress ia) {
        Ping ping = new Ping();
        ping.setAddress(ia);
        return ping;
    }

    /**
     * Set the timeout
     *
     * @param timeOutMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    public Ping setTimeOutMillis(int timeOutMillis) {
        if (timeOutMillis < 0) throw new IllegalArgumentException("Times cannot be less than 0");
        pingOptions.setTimeoutMillis(timeOutMillis);
        return this;
    }

    /**
     * Set the delay between each ping
     *
     * @param delayBetweenScansMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    public Ping setDelayMillis(int delayBetweenScansMillis) {
        if (delayBetweenScansMillis < 0)
            throw new IllegalArgumentException("Delay cannot be less than 0");
        this.delayBetweenScansMillis = delayBetweenScansMillis;
        return this;
    }

    /**
     * Set the time to live
     *
     * @param timeToLive - the TTL for each ping
     * @return this object to allow chaining
     */
    public Ping setTimeToLive(int timeToLive) {
        if (timeToLive < 1) throw new IllegalArgumentException("TTL cannot be less than 1");
        pingOptions.setTimeToLive(timeToLive);
        return this;
    }

    /**
     * Set number of times to ping the address
     *
     * @param noTimes - number of times, 0 = continuous
     * @return this object to allow chaining
     */
    public Ping setTimes(int noTimes) {
        if (noTimes < 0) throw new IllegalArgumentException("Times cannot be less than 0");
        this.times = noTimes;
        return this;
    }

    private void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Set the address string which will be resolved to an address by resolveAddressString()
     *
     * @param addressString - String of the address to be pinged
     */
    private void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    /**
     * Parses the addressString to an address
     *
     * @throws UnknownHostException - if host cannot be found
     */
    private void resolveAddressString() throws UnknownHostException {
        if (address == null && addressString != null) {
            address = InetAddress.getByName(addressString);
        }
    }

    /**
     * Cancel a running ping
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Perform a synchronous ping and return a result, will ignore number of times.
     *
     * Note that this should be performed on a background thread as it will perform a network
     * request
     *
     * @return - ping result
     * @throws UnknownHostException - if the host cannot be resolved
     */
    public PingResult doPing() throws UnknownHostException {
        cancelled = false;
        resolveAddressString();
        return PingTools.doPing(address, pingOptions);
    }

    /**
     * Perform an asynchronous ping
     *
     * @param pingListener - the listener to fire PingResults to.
     * @return - this so we can cancel if needed
     */
    public Ping doPing(final PingListener pingListener) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    resolveAddressString();
                } catch (UnknownHostException e) {
                    pingListener.onError(e);
                    return;
                }

                if (address == null) {
                    pingListener.onError(new NullPointerException("Address is null"));
                    return;
                }

                long pingsCompleted = 0;
                long noLostPackets = 0;
                float totalPingTime = 0;
                float minPingTime = -1;
                float maxPingTime = -1;

                cancelled = false;
                int noPings = times;

                // times == 0 is the case that we can continuous scanning
                while (noPings > 0 || times == 0) {
                    PingResult pingResult = PingTools.doPing(address, pingOptions);

                    if (pingListener != null) {
                        pingListener.onResult(pingResult);
                    }

                    // Update ping stats
                    pingsCompleted++;

                    if (pingResult.hasError()) {
                        noLostPackets++;
                    } else {
                        float timeTaken = pingResult.getTimeTaken();
                        totalPingTime += timeTaken;
                        if (maxPingTime == -1 || timeTaken > maxPingTime) maxPingTime = timeTaken;
                        if (minPingTime == -1 || timeTaken < minPingTime) minPingTime = timeTaken;
                    }

                    noPings--;
                    if (cancelled) break;

                    try {
                        Thread.sleep(delayBetweenScansMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (pingListener != null) {
                    pingListener.onFinished(new PingStats(address, pingsCompleted, noLostPackets, totalPingTime, minPingTime, maxPingTime));
                }
            }
        }).start();
        return this;
    }

}