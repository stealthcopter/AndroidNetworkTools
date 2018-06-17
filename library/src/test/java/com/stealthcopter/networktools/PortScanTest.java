package com.stealthcopter.networktools;

import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PortScanTest {
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidTimout() throws Exception {
        PortScan.onAddress("127.0.0.1").setTimeOutMillis(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPortLow() throws Exception {
        PortScan.onAddress("127.0.0.1").setPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidPortHigh() throws Exception {
        PortScan.onAddress("127.0.0.1").setPort(65536);
    }
}