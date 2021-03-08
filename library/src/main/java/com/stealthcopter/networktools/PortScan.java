package com.stealthcopter.networktools;

import com.stealthcopter.networktools.portscanning.PortScanTCP;
import com.stealthcopter.networktools.portscanning.PortScanUDP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PortScan {

    private static final int TIMEOUT_LOCALHOST = 25;
    private static final int TIMEOUT_LOCALNETWORK = 1000;
    private static final int TIMEOUT_REMOTE = 2500;

    private static final int DEFAULT_THREADS_LOCALHOST = 7;
    private static final int DEFAULT_THREADS_LOCALNETWORK = 50;
    private static final int DEFAULT_THREADS_REMOTE = 50;

    private static final int METHOD_TCP = 0;
    private static final int METHOD_UDP = 1;

    private int method = METHOD_TCP;
    private int noThreads = 50;
    private InetAddress address;
    private int timeOutMillis = 1000;
    private boolean cancelled = false;

    private ArrayList<Integer> ports = new ArrayList<>();
    private ArrayList<Integer> openPortsFound = new ArrayList<>();

    private PortListener portListener;

    // This class is not to be instantiated
    private PortScan() {
    }

    public interface PortListener {
        void onResult(int portNo, boolean open);

        void onFinished(ArrayList<Integer> openPorts);
    }

    /**
     * Set the address to ping
     *
     * @param address - Address to be pinged
     * @return this object to allow chaining
     * @throws UnknownHostException - if no IP address for the
     *                              {@code host} could be found, or if a scope_id was specified
     *                              for a global IPv6 address.
     */
    public static PortScan onAddress(String address) throws UnknownHostException {
        return onAddress(InetAddress.getByName(address));
    }

    /**
     * Set the address to ping
     *
     * @param ia - Address to be pinged
     * @return this object to allow chaining
     */
    public static PortScan onAddress(InetAddress ia) {
        PortScan portScan = new PortScan();
        portScan.setAddress(ia);
        portScan.setDefaultThreadsAndTimeouts();
        return portScan;
    }

    /**
     * Sets the timeout for each port scanned
     * <p>
     * If you raise the timeout you may want to consider increasing the thread count {@link #setNoThreads(int)} to compensate.
     * We can afford to have quite a high thread count as most of the time the thread is just sitting
     * idle and waiting for the socket to timeout.
     *
     * @param timeOutMillis - the timeout for each ping in milliseconds
     *                      Recommendations:
     *                      Local host: 20 - 500 ms - can be very fast as request doesn't need to go over network
     *                      Local network 500 - 2500 ms
     *                      Remote Scan 2500+ ms
     * @return this object to allow chaining
     */
    public PortScan setTimeOutMillis(int timeOutMillis) {
        if (timeOutMillis < 0) throw new IllegalArgumentException("Timeout cannot be less than 0");
        this.timeOutMillis = timeOutMillis;
        return this;
    }

    /**
     * Scan the ports to scan
     *
     * @param port - the port to scan
     * @return this object to allow chaining
     */
    public PortScan setPort(int port) {
        ports.clear();
        validatePort(port);
        ports.add(port);
        return this;
    }

    /**
     * Scan the ports to scan
     *
     * @param ports - the ports to scan
     * @return this object to allow chaining
     */
    public PortScan setPorts(ArrayList<Integer> ports) {

        // Check all ports are valid
        for (Integer port : ports) {
            validatePort(port);
        }

        this.ports = ports;

        return this;
    }

    /**
     * Scan the ports to scan
     *
     * @param portString - the ports to scan (comma separated, hyphen denotes a range). For example:
     *                   "21-23,25,45,53,80"
     * @return this object to allow chaining
     */
    public PortScan setPorts(String portString) {

        ports.clear();

        ArrayList<Integer> ports = new ArrayList<>();

        if (portString == null) {
            throw new IllegalArgumentException("Empty port string not allowed");
        }

        portString = portString.substring(portString.indexOf(":") + 1, portString.length());

        for (String x : portString.split(",")) {
            if (x.contains("-")) {
                int start = Integer.parseInt(x.split("-")[0]);
                int end = Integer.parseInt(x.split("-")[1]);
                validatePort(start);
                validatePort(end);
                if (end <= start)
                    throw new IllegalArgumentException("Start port cannot be greater than or equal to the end port");

                for (int j = start; j <= end; j++) {
                    ports.add(j);
                }
            } else {
                int start = Integer.parseInt(x);
                validatePort(start);
                ports.add(start);
            }
        }

        this.ports = ports;

        return this;
    }

    /**
     * Checks and throws exception if port is not valid
     *
     * @param port - the port to validate
     */
    private void validatePort(int port) {
        if (port < 1) throw new IllegalArgumentException("Start port cannot be less than 1");
        if (port > 65535) throw new IllegalArgumentException("Start cannot be greater than 65535");
    }

    /**
     * Scan all privileged ports
     *
     * @return this object to allow chaining
     */
    public PortScan setPortsPrivileged() {
        ports.clear();
        for (int i = 1; i < 1024; i++) {
            ports.add(i);
        }
        return this;
    }

    /**
     * Scan all ports
     *
     * @return this object to allow chaining
     */
    public PortScan setPortsAll() {
        ports.clear();
        for (int i = 1; i < 65536; i++) {
            ports.add(i);
        }
        return this;
    }

    private void setAddress(InetAddress address) {
        this.address = address;
    }

    private void setDefaultThreadsAndTimeouts() {
        // Try and work out automatically what kind of host we are scanning
        // local host (this device) / local network / remote
        if (IPTools.isIpAddressLocalhost(address)) {
            // If we are scanning a the localhost set the timeout to be very short so we get faster results
            // This will be overridden if user calls setTimeoutMillis manually.
            timeOutMillis = TIMEOUT_LOCALHOST;
            noThreads = DEFAULT_THREADS_LOCALHOST;
        } else if (IPTools.isIpAddressLocalNetwork(address)) {
            // Assume local network (not infallible)
            timeOutMillis = TIMEOUT_LOCALNETWORK;
            noThreads = DEFAULT_THREADS_LOCALNETWORK;
        } else {
            // Assume remote network timeouts
            timeOutMillis = TIMEOUT_REMOTE;
            noThreads = DEFAULT_THREADS_REMOTE;
        }
    }

    /**
     * @param noThreads set the number of threads to work with, note we default to a large number
     *                  as these requests are network heavy not cpu heavy.
     * @return self
     * @throws IllegalArgumentException - if no threads is less than 1
     */
    public PortScan setNoThreads(int noThreads) throws IllegalArgumentException {
        if (noThreads < 1) throw new IllegalArgumentException("Cannot have less than 1 thread");
        this.noThreads = noThreads;
        return this;
    }


    /**
     * Set scan method, either TCP or UDP
     *
     * @param method - the transport method to use to scan, either PortScan.METHOD_UDP or PortScan.METHOD_TCP
     * @return this object to allow chaining
     * @throws IllegalArgumentException - if invalid method
     */
    private PortScan setMethod(int method) {
        switch (method) {
            case METHOD_UDP:
            case METHOD_TCP:
                this.method = method;
                break;
            default:
                throw new IllegalArgumentException("Invalid method type " + method);
        }
        return this;
    }

    /**
     * Set scan method to UDP
     *
     * @return this object to allow chaining
     */
    public PortScan setMethodUDP() {
        setMethod(METHOD_UDP);
        return this;
    }

    /**
     * Set scan method to TCP
     *
     * @return this object to allow chaining
     */
    public PortScan setMethodTCP() {
        setMethod(METHOD_TCP);
        return this;
    }


    /**
     * Cancel a running ping
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Perform a synchronous (blocking) port scan and return a list of open ports
     *
     * @return - ping result
     */
    public ArrayList<Integer> doScan() {

        cancelled = false;
        openPortsFound.clear();

        ExecutorService executor = Executors.newFixedThreadPool(noThreads);

        for (int portNo : ports) {
            Runnable worker = new PortScanRunnable(address, portNo, timeOutMillis, method);
            executor.execute(worker);
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Collections.sort(openPortsFound);

        return openPortsFound;
    }

    /**
     * Perform an asynchronous (non-blocking) port scan
     *
     * @param portListener - the listener to fire portscan results to.
     * @return - this object so we can cancel the scan if needed
     */
    public PortScan doScan(final PortListener portListener) {

        this.portListener = portListener;
        openPortsFound.clear();
        cancelled = false;

        new Thread(new Runnable() {
            @Override
            public void run() {

                ExecutorService executor = Executors.newFixedThreadPool(noThreads);

                for (int portNo : ports) {
                    Runnable worker = new PortScanRunnable(address, portNo, timeOutMillis, method);
                    executor.execute(worker);
                }

                // This will make the executor accept no new threads
                // and finish all existing threads in the queue
                executor.shutdown();
                // Wait until all threads are finish
                try {
                    executor.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (portListener != null) {
                    Collections.sort(openPortsFound);
                    portListener.onFinished(openPortsFound);
                }

            }
        }).start();

        return this;
    }

    private synchronized void portScanned(int port, boolean open) {
        if (open) {
            openPortsFound.add(port);
        }
        if (portListener != null) {
            portListener.onResult(port, open);
        }
    }

    private class PortScanRunnable implements Runnable {
        private final InetAddress address;
        private final int portNo;
        private final int timeOutMillis;
        private final int method;

        PortScanRunnable(InetAddress address, int portNo, int timeOutMillis, int method) {
            this.address = address;
            this.portNo = portNo;
            this.timeOutMillis = timeOutMillis;
            this.method = method;
        }

        @Override
        public void run() {
            if (cancelled) return;

            switch (method) {
                case METHOD_UDP:
                    portScanned(portNo, PortScanUDP.scanAddress(address, portNo, timeOutMillis));
                    break;
                case METHOD_TCP:
                    portScanned(portNo, PortScanTCP.scanAddress(address, portNo, timeOutMillis));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid method");
            }
        }
    }


}