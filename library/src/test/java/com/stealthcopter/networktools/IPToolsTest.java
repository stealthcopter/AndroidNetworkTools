package com.stealthcopter.networktools;

import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthew on 03/11/17.
 */

public class IPToolsTest {


    String[] getInvalidIpAddresses() {
        return new String[]{null, "beepbeep", "nope", "hello"};
    }

    String[] getIPv4Addresses() {
        return new String[]{"192.168.0.1", "127.0.0.1", "10.0.0.1",};
    }

    String[] getIPv6Addresses() {
        return new String[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334"};
    }

    String[] getIPv6AddressesHexCompresed() {
        return new String[]{"2001:db8:85a3::8a2e:370:7334", "2001::8a2e:370:7334", "2001:", "2001::2001"};
    }

    @Test
    public void testIsIPv4Address() {

        for (String address : getIPv4Addresses()) {
            assertTrue(IPTools.isIPv4Address(address));
        }

        for (String address : getIPv6Addresses()) {
            assertFalse(IPTools.isIPv4Address(address));
        }

        for (String address : getInvalidIpAddresses()) {
            assertFalse(IPTools.isIPv4Address(address));
        }
    }

    @Test
    public void testIsIPv6Address() {
        for (String address : getIPv4Addresses()) {
            assertFalse(IPTools.isIPv6Address(address));
        }

        for (String address : getIPv6Addresses()) {
            assertTrue(IPTools.isIPv6Address(address));
        }

        for (String address : getInvalidIpAddresses()) {
            assertFalse(IPTools.isIPv6Address(address));
        }
    }

    @Test
    public void testIsIPv6AddressesStandard() {
        for (String address : getIPv4Addresses()) {
            assertFalse(IPTools.isIPv6StdAddress(address));
        }

        for (String address : getIPv6Addresses()) {
            assertTrue(IPTools.isIPv6StdAddress(address));
        }

        for (String address : getInvalidIpAddresses()) {
            assertFalse(IPTools.isIPv6StdAddress(address));
        }
    }

    @Test
    @Ignore  // Recheck this, either test is broken or regex is wrong
    public void testIPv6HexCompressedAddress() {
        for (String address : getIPv6AddressesHexCompresed()) {
            assertTrue(IPTools.isIPv6HexCompressedAddress(address));
        }
    }

    @Test
    public void testGetLocalAddressReturnsLocalIP() {
        InetAddress test = IPTools.getLocalIPv4Address();

        assertNotNull(test);

        assertTrue(IPTools.isIpAddressLocalhost(test));
        assertTrue(IPTools.isIpAddressLocalNetwork(test));
    }


    @Test
    public void testGetAllLocalAddressReturnsLocalIP() {
        List<InetAddress> test = IPTools.getLocalIPv4Addresses();

        for (InetAddress address : test) {
            System.out.println(address);
            assertNotNull(address);

            assertTrue(IPTools.isIpAddressLocalhost(address));
            assertTrue(IPTools.isIpAddressLocalNetwork(address));
        }
    }

    @Test
    public void testLocalAddresses() throws UnknownHostException {
        assertTrue(IPTools.isIpAddressLocalhost(InetAddress.getByName("127.0.0.1")));
        assertFalse(IPTools.isIpAddressLocalhost(InetAddress.getByName("8.8.8.8")));
    }

    @Test
    public void testLocalAddressesNetwork() throws UnknownHostException {
        assertFalse(IPTools.isIpAddressLocalNetwork(InetAddress.getByName("8.8.8.8")));
    }

}
