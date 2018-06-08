package com.stealthcopter.networktools;

import org.junit.Test;

import java.net.InetAddress;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class WakeOnLanTest {

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidIPaddressStr() throws Exception {
        WakeOnLan.sendWakeOnLan((String)null, "00:04:20:06:55:1a", 9, 10000, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidIPaddress() throws Exception {
        WakeOnLan.sendWakeOnLan((InetAddress) null, "00:04:20:06:55:1a", 9, 10000, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidMACaddress() throws Exception {
        WakeOnLan.sendWakeOnLan("192.168.0.1", null, 9, 10000, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPortLow() throws Exception {
        WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", -1, 10000, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPortHigh() throws Exception {
        WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", 65536, 10000, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPackets() throws Exception {
        WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", 9, 10000, 0);
    }
}