package com.stealthcopter.networktools;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthew on 03/11/17.
 */

public class MACToolsTest {

    String[] getInvalidMACAddresses(){
        return new String[]{null, "beepbeep", "nope", "hello", "00-15-E9-2B-99+3C", "0G-15-E9-2B-99-3C"};
    }

    String[] getValidMACAddresses(){
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
        for (String macAddress: getInvalidMACAddresses()) {
            assertFalse(MACTools.isValidMACAddress(macAddress));
        }
    }

}
