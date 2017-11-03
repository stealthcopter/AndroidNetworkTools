package com.stealthcopter.networktools;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthew on 03/11/17.
 */

public class IPToolsTest {


    String[] getInvalidIpAddresses(){
        return new String[]{"beepbeep", "nope", "hello"};
    }

    String[] getIPv4Addresses(){
        return new String[]{"192.168.0.1", "127.0.0.1", "10.0.0.1",};
    }

    String[] getIPv6Addresses(){
        return new String[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334"};
    }

    @Test
    public void testIsIPv4Address() throws Exception {

        for (String address: getIPv4Addresses()) {
            assertTrue(IPTools.isIPv4Address(address));
        }

        for (String address: getIPv6Addresses()) {
            assertFalse(IPTools.isIPv4Address(address));
        }

        for (String address: getInvalidIpAddresses()) {
            assertFalse(IPTools.isIPv4Address(address));
        }
    }

    @Test
    public void testIsIPv6Address() throws Exception {
        for (String address: getIPv4Addresses()) {
            assertFalse(IPTools.isIPv6Address(address));
        }

        for (String address: getIPv6Addresses()) {
            assertTrue(IPTools.isIPv6Address(address));
        }

        for (String address: getInvalidIpAddresses()) {
            assertFalse(IPTools.isIPv6Address(address));
        }
    }
}
