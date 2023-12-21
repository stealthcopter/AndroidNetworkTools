package com.stealthcopter.networktools

import junit.framework.Assert
import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class ARPInfoTest {
    @Test
    fun nullIPsandMacsReturnNull() {
        Assert.assertNull(ARPInfo.getMACFromIPAddress(null))
        Assert.assertNull(ARPInfo.getIPAddressFromMAC(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIllegalArgumentThrownOnInvalidMACaddress() {
        ARPInfo.getIPAddressFromMAC("00:00:00:xx")
    }
}