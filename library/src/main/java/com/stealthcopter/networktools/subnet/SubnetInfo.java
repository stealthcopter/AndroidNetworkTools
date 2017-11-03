package com.stealthcopter.networktools.subnet;

import java.net.InetAddress;

/**
 * Class: PortInfo
 * <p/>
 * <p/>
 * <p/>
 * Project: com.stealthcopter.portdroid portdroid
 * Created Date: 19/02/14 02:30
 *
 * @author <a href="mailto:matthew.rollings@intohand.com">Matthew Rollings</a>
 *         Copyright (c) 2014 Intohand Ltd. All rights reserved.
 */
public class SubnetInfo {
    public String ip = "";
    public String hostname = "";
    public String mac = "";


    public float time = 0;

    public SubnetInfo(InetAddress ip) {
        this.ip = ip.getHostAddress();
        this.hostname = ip.getCanonicalHostName();
    }

    @Override
    public String toString() {
        return "SubnetInfo{" +
                "ip='" + ip + '\'' +
                ", hostname='" + hostname + '\'' +
                ", mac='" + mac + '\'' +
                ", time=" + time +
                '}';
    }
}

