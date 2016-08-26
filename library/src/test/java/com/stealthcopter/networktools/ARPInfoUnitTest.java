package com.stealthcopter.networktools;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ARPInfoUnitTest {

    @Test
    public void nullIPsandMacsReturnNull() throws Exception {

        assertNull(ARPInfo.getMACFromIPAddress(null));
        assertNull(ARPInfo.getIPAddressFromMAC(null));

//        assertEquals(arpInfo.getMACFromIPAddress("192.168.18.11"), "00:04:20:06:55:1a");
//        assertEquals(arpInfo.getIPAddressFromMAC("00:22:43:ab:2a:5b"), "192.168.18.36");

    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentThrownOnInvalidMACaddress(){
        ARPInfo.getIPAddressFromMAC("00:00:00:xx");
    }
}