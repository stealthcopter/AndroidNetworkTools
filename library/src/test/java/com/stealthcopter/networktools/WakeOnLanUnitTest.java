package com.stealthcopter.networktools;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNull;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class WakeOnLanUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidIPaddress(){
        try {
            WakeOnLan.sendWakeOnLan(null, "00:04:20:06:55:1a", 9, 10000, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidMACaddress(){
        try {
            WakeOnLan.sendWakeOnLan("192.168.0.1", null, 9, 10000, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPortLow(){
        try {
            WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", -1, 10000, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPortHigh(){
        try {
            WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", 65536, 10000, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPackets(){
        try {
            WakeOnLan.sendWakeOnLan("192.168.0.1", "00:04:20:06:55:1a", 9, 10000, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}