package com.stealthcopter.networktools;

import android.support.annotation.NonNull;

import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.stealthcopter.networktools.ping.PingTools;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by mat on 09/12/15.
 */
public class Ping {

    public interface PingListener{
        void onResult(PingResult pingResult);
        void onFinished(PingStats pingStats);
    }

    private InetAddress address;
    private int timeOutMillis = 1000;
    private int delayBetweenScansMillis = 0;
    private int times = 1;
    private boolean cancelled = false;

    /**
     * Set the address to ping
     * @param address - Address to be pinged
     * @return this object to allow chaining
     * @throws UnknownHostException
     */
    public static Ping onAddress(@NonNull String address) throws UnknownHostException {
        Ping ping = new Ping();
        InetAddress ia = InetAddress.getByName(address);
        ping.setAddress(ia);
        return ping;
    }

    /**
     * Set the address to ping
     * @param ia - Address to be pinged
     * @return this object to allow chaining
     */
    public static Ping onAddress(@NonNull InetAddress ia) {
        Ping ping = new Ping();
        ping.setAddress(ia);
        return ping;
    }

    /**
     * Set the timeout
     * @param timeOutMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    public Ping setTimeOutMillis(int timeOutMillis){
        if (timeOutMillis<0) throw new IllegalArgumentException("Times cannot be less than 0");
        this.timeOutMillis = timeOutMillis;
        return this;
    }

    /**
     * Set the delay between each ping
     * @param delayBetweenScansMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    public Ping setDelayMillis(int delayBetweenScansMillis){
        if (delayBetweenScansMillis<0) throw new IllegalArgumentException("Delay cannot be less than 0");
        this.delayBetweenScansMillis = delayBetweenScansMillis;
        return this;
    }

    /**
     * Set number of times to ping the address
     * @param noTimes - number of times, 0 = continuous
     * @return this object to allow chaining
     */
    public Ping setTimes(int noTimes){
        if (noTimes<0) throw new IllegalArgumentException("Times cannot be less than 0");
        this.times = noTimes;
        return this;
    }

    private void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Cancel a running ping
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Perform a synchronous ping and return a result, will ignore number of times.
     * @return - ping result
     */
    public PingResult doPing(){
        cancelled = false;
        return PingTools.doPing(address, timeOutMillis);
    }

    /**
     * Perform an asynchronous ping
     * @param pingListener - the listener to fire PingResults to.
     * @return - this so we can cancel if needed
     */
    public Ping doPing(final PingListener pingListener){

        new Thread(new Runnable() {
            @Override
            public void run() {

                long pingsCompleted = 0;
                long noLostPackets = 0;
                float totalPingTime = 0;
                float minPingTime = -1;
                float maxPingTime = -1;

                cancelled = false;
                int noPings = times;

                // times == 0 is the case that we can continuous scanning
                while(noPings>0 || times == 0){
                    PingResult pingResult = PingTools.doPing(address, timeOutMillis);

                    if (pingListener!=null){
                        pingListener.onResult(pingResult);
                    }

                    // Update ping stats
                    pingsCompleted++;

                    if (pingResult.hasError()){
                        noLostPackets++;
                    }
                    else{
                        float timeTaken = pingResult.getTimeTaken();
                        totalPingTime += timeTaken;
                        if (maxPingTime == - 1 || timeTaken > maxPingTime) maxPingTime = timeTaken;
                        if (minPingTime == - 1 || timeTaken < minPingTime) minPingTime = timeTaken;
                    }

                    noPings--;
                    if (cancelled) break;

                    try {
                        Thread.sleep(delayBetweenScansMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (pingListener!=null){
                    pingListener.onFinished(new PingStats(address, pingsCompleted, noLostPackets, totalPingTime, minPingTime, maxPingTime));
                }
            }
        }).start();
        return this;
    }

}
