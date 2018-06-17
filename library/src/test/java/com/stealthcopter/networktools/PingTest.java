package com.stealthcopter.networktools;

import org.junit.Test;

/**
 * Created by matthew on 03/11/17.
 */

public class PingTest {

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidDelay() throws Exception {
        Ping.onAddress("127.0.0.1").setDelayMillis(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidTimeout() throws Exception {
        Ping.onAddress("127.0.0.1").setTimeOutMillis(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidTimes() throws Exception {
        Ping.onAddress("127.0.0.1").setTimes(-1);
    }

}
