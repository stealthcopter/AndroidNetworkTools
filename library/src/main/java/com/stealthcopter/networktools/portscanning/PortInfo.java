package com.stealthcopter.networktools.portscanning;

public class PortInfo {
    public String ip;
    public int portNo;
    public String openState;
    public boolean open;

    public PortInfo(String ip, int portNo) {
        this.portNo = portNo;
        this.ip = ip;
    }

    public boolean isOpen(){return open;}
}