package com.stealthcopter.networktools.subnet

import java.net.InetAddress

class Device(ip: InetAddress) {
    @JvmField
    var ip: String
    var hostname: String
    @JvmField
    var mac: String? = null
    @JvmField
    var time = 0f

    init {
        this.ip = ip.hostAddress
        hostname = ip.canonicalHostName
    }

    override fun toString(): String {
        return "Device{" +
                "ip='" + ip + '\'' +
                ", hostname='" + hostname + '\'' +
                ", mac='" + mac + '\'' +
                ", time=" + time +
                '}'
    }
}