package com.stealthcopter.networktools.ping;

import android.text.TextUtils;

import java.net.InetAddress;

/**
 * Created by mat on 09/12/15.
 */
public class PingResult {
    public final InetAddress ia;
    public boolean isReachable;
    public String error = null;
    public float timeTaken;
    public String fullString;
    public String result;

    public PingResult(InetAddress ia){
        this.ia=ia;
    }

    public boolean isReachable(){
        return isReachable;
    }

    public boolean hasError(){
        return error != null;
    }

    public float getTimeTaken(){
        return timeTaken;
    }

    public String getError(){
        return error;
    }

    public InetAddress getAddress(){
        return ia;
    }

    @Override
    public String toString() {
        return "PingResult{" +
                "ia=" + ia +
                ", isReachable=" + isReachable +
                ", error='" + error + '\'' +
                ", timeTaken=" + timeTaken +
                ", fullString='" + fullString + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
