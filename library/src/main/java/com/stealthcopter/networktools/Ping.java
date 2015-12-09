package com.stealthcopter.networktools;

import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingTools;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by mat on 09/12/15.
 */
public class Ping {

    private InetAddress address;
    private int timeOutMillis = 1000;

    public static Ping onAddress(String address) throws UnknownHostException {
        Ping ping = new Ping();
        InetAddress ia = InetAddress.getByName(address);
        ping.setAddress(ia);
        return ping;
    }

    public static Ping onAddress(InetAddress ia) {
        Ping ping = new Ping();
        ping.setAddress(ia);
        return ping;
    }


    public Ping setTimeOutMillis(int timeOutMillis){
        this.timeOutMillis = timeOutMillis;
        return this;
    }


    private void setAddress(InetAddress address) {
        this.address = address;
    }

    public PingResult doPing(){
        return PingTools.doPing(address, timeOutMillis);
    }

}
