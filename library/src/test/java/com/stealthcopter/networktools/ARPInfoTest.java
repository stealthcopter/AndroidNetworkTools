package com.stealthcopter.networktools;

import org.junit.Test;

import static junit.framework.Assert.assertNull;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ARPInfoTest {

    @Test
    public void nullIPsandMacsReturnNull() {
        assertNull(ARPInfo.getMACFromIPAddress(null));
        assertNull(ARPInfo.getIPAddressFromMAC(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidMACaddress() {
        ARPInfo.getIPAddressFromMAC("00:00:00:xx");
    }
}