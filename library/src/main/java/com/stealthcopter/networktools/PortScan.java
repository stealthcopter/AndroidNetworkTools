package com.stealthcopter.networktools;

import android.support.annotation.NonNull;

import com.stealthcopter.networktools.portscanning.PortScanTCP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by mat on 14/12/15.
 */
public class PortScan {

    public interface PortListener{
        void onResult(int portNo, boolean open);
        void onFinished(ArrayList<Integer> openPorts);
    }

    private InetAddress address;
    private int timeOutMillis = 1000;
    private boolean cancelled = false;
    private ArrayList<Integer> ports = new ArrayList<>();

    /**
     * Set the address to ping
     * @param address - Address to be pinged
     * @return this object to allow chaining
     * @throws UnknownHostException
     */
    public static PortScan onAddress(@NonNull String address) throws UnknownHostException {
        PortScan portScan = new PortScan();
        InetAddress ia = InetAddress.getByName(address);
        portScan.setAddress(ia);
        return portScan;
    }

    /**
     * Set the address to ping
     * @param ia - Address to be pinged
     * @return this object to allow chaining
     */
    public static PortScan onAddress(@NonNull InetAddress ia) {
        PortScan portScan = new PortScan();
        portScan.setAddress(ia);
        return portScan;
    }

    /**
     * Set the timeout
     * @param timeOutMillis - the timeout for each ping in milliseconds
     * @return this object to allow chaining
     */
    public PortScan setTimeOutMillis(int timeOutMillis){
        if (timeOutMillis<0) throw new IllegalArgumentException("Times cannot be less than 0");
        this.timeOutMillis = timeOutMillis;
        return this;
    }

    /**
     * Scan the ports to scan
     * @param port - the port to scan
     * @return this object to allow chaining
     */
    public PortScan setPort(int port){
        ports.clear();
        if (port<1) throw new IllegalArgumentException("Port cannot be less than 1");
        else if (port>65535) throw new IllegalArgumentException("Port cannot be greater than 65535");
        ports.add(port);
        return this;
    }

    /**
     * Scan the ports to scan
     * @param ports - the ports to scan
     * @return this object to allow chaining
     */
    public PortScan setPorts(ArrayList<Integer> ports){
        ports.clear();
        // TODO: Validate / sanitize
        this.ports = ports;

        return this;
    }

    /**
     * Scan the ports to scan
     * @param portString - the ports to scan (comma separated, hyphen denotes a range). For example:
     *                               "21-23,25,45,53,80"
     * @return this object to allow chaining
     */
    public PortScan setPorts(String portString){

        ports.clear();

        ArrayList<Integer> ports = new ArrayList<>();

        if (portString==null){
            throw new IllegalArgumentException("Empty port string not allowed");
        }

        portString = portString.substring(portString.indexOf(":")+1, portString.length());

        for (String x : portString.split(",")){
            if (x.contains("-")){
                int start = Integer.parseInt(x.split("-")[0]);
                int end = Integer.parseInt(x.split("-")[1]);
                if (start<1) throw new IllegalArgumentException("Start port cannot be less than 1");
                if (start>65535) throw new IllegalArgumentException("Start cannot be greater than 65535");
                if (end>65535) throw new IllegalArgumentException("Start cannot be greater than 65535");
                if (end<=start) throw new IllegalArgumentException("Start port cannot be greater than or equal to the end port");

                for (int j=start; j<=end;j++){
                    ports.add(j);
                }
            }
            else{
                int start = Integer.parseInt(x);
                if (start<1) throw new IllegalArgumentException("Start port cannot be less than 1");
                if (start>65535) throw new IllegalArgumentException("Start cannot be greater than 65535");
                ports.add(start);
            }
        }

        this.ports = ports;

        return this;
    }

    /**
     * Scan all privileged ports
     * @return this object to allow chaining
     */
    public PortScan setPortsPrivileged(){
        ports.clear();
        for (int i = 1; i < 1024; i++) {
            ports.add(i);
        }
        return this;
    }

    /**
     * Scan all ports
     * @return this object to allow chaining
     */
    public PortScan setPortsAll(){
        ports.clear();
        for (int i = 1; i < 65535; i++) {
            ports.add(i);
        }
        return this;
    }

    private void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Cancel a running ping
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Perform a synchrnous port scan and return a list of open ports
     * @return - ping result
     */
    public ArrayList<Integer> doScan(){

        cancelled = false;

        ArrayList<Integer> openPorts = new ArrayList<>();

        for (int portNo : ports) {
            if (PortScanTCP.scanAddress(address, portNo, timeOutMillis)){
                openPorts.add(portNo);
            }
            if (cancelled) break;
        }

        return openPorts;
    }

    /**
     * Perform an asynchronous port scan
     * @param portListener - the listener to fire portscan results to.
     * @return - this object so we can cancel the scan if needed
     */
    public PortScan doScan(final PortListener portListener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                cancelled = false;

                ArrayList<Integer> openPorts = new ArrayList<>();
                for (int portNo : ports) {
                    boolean open = PortScanTCP.scanAddress(address, portNo, 1000);
                    if (portListener!=null){
                        portListener.onResult(portNo, open);
                        if (open) openPorts.add(portNo);
                    }
                    if (cancelled) break;
                }

                if (portListener!=null){
                    portListener.onFinished(openPorts);
                }

            }
        }).start();

        return this;
    }


}
