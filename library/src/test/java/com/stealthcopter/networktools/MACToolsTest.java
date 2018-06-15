package com.stealthcopter.networktools;

import org.junit.Test;
import org.mockito.internal.matchers.Null;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthew on 03/11/17.
 */

public class MACToolsTest {

    String[] getInvalidMACAddresses() {
        return new String[]{null, "beepbeep", "nope", "hello", "00-15-E9-2B-99+3C", "0G-15-E9-2B-99-3C"};
    }

    String[] getValidMACAddresses() {
        return new String[]{"00:00:00:00:00:00", "00-15-E9-2B-99-3C", "00:15:E9:2B:99:3C", "00-15-e9-2b-99-3c"};
    }

    @Test
    public void testValidMACAddresses() {
        for (String macAddress : getValidMACAddresses()) {
            assertTrue(MACTools.isValidMACAddress(macAddress));
        }
    }

    @Test
    public void testInvalidMACAddresses() {
        for (String macAddress : getInvalidMACAddresses()) {
            assertFalse(MACTools.isValidMACAddress(macAddress));
        }
    }

    @Test
    public void testMACGetBytes() {
        byte[] bytes = MACTools.getMacBytes("01:02:03:04:05:06");

        assertEquals(0x01, bytes[0]);
        assertEquals(0x02, bytes[1]);
        assertEquals(0x03, bytes[2]);
        assertEquals(0x04, bytes[3]);
        assertEquals(0x05, bytes[4]);
        assertEquals(0x06, bytes[5]);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testMACGetBytesThrowsNull() {
        MACTools.getMacBytes(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMACGetBytesThrowInvalidMac() {
        MACTools.getMacBytes("00:00:00");
    }
}
