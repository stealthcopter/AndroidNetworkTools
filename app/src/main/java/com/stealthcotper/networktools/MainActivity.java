package com.stealthcotper.networktools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stealthcopter.networktools.ARPInfo;
import com.stealthcopter.networktools.IPTools;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.WakeOnLan;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.stealthcopter.networktools.subnet.Device;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView resultText;
    private EditText editIpAddress;
    private ScrollView scrollView;
    private Button pingButton;
    private Button wolButton;
    private Button portScanButton;
    private Button subnetDevicesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultText = findViewById(R.id.resultText);
        editIpAddress = findViewById(R.id.editIpAddress);
        scrollView = findViewById(R.id.scrollView1);
        pingButton = findViewById(R.id.pingButton);
        wolButton = findViewById(R.id.wolButton);
        portScanButton = findViewById(R.id.portScanButton);
        subnetDevicesButton = findViewById(R.id.subnetDevicesButton);

        InetAddress ipAddress = IPTools.getLocalIPv4Address();
        if (ipAddress != null){
            editIpAddress.setText(ipAddress.getHostAddress());
        }

        findViewById(R.id.pingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPing();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.wolButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doWakeOnLan();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.portScanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPortScan();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.subnetDevicesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            findSubnetDevices();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    private void appendResultsText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.append(text + "\n");
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void setEnabled(final View view, final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    view.setEnabled(enabled);
                }
            }
        });
    }

    private void doPing() throws Exception {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            return;
        }

        setEnabled(pingButton, false);

        // Perform a single synchronous ping
        PingResult pingResult = null;
        try {
            pingResult = Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            appendResultsText(e.getMessage());
            setEnabled(pingButton, true);
            return;
        }


        appendResultsText("Pinging Address: " + pingResult.getAddress().getHostAddress());
        appendResultsText("HostName: " + pingResult.getAddress().getHostName());
        appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));


        // Perform an asynchronous ping
        Ping.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(5).doPing(new Ping.PingListener() {
            @Override
            public void onResult(PingResult pingResult) {
                if (pingResult.isReachable) {
                    appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));
                } else {
                    appendResultsText(getString(R.string.timeout));
                }
            }

            @Override
            public void onFinished(PingStats pingStats) {
                appendResultsText(String.format("Pings: %d, Packets lost: %d",
                        pingStats.getNoPings(), pingStats.getPacketsLost()));
                appendResultsText(String.format("Min/Avg/Max Time: %.2f/%.2f/%.2f ms",
                        pingStats.getMinTimeTaken(), pingStats.getAverageTimeTaken(), pingStats.getMaxTimeTaken()));
                setEnabled(pingButton, true);
            }

            @Override
            public void onError(Exception e) {
                // TODO: STUB METHOD
                setEnabled(pingButton, true);
            }
        });

    }

    private void doWakeOnLan() throws IllegalArgumentException {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            return;
        }

        setEnabled(wolButton, false);

        appendResultsText("IP address: " + ipAddress);

        // Get mac address from IP (using arp cache)
        String macAddress = ARPInfo.getMACFromIPAddress(ipAddress);

        if (macAddress == null) {
            appendResultsText("Could not fromIPAddress MAC address, cannot send WOL packet without it.");
            setEnabled(wolButton, true);
            return;
        }

        appendResultsText("MAC address: " + macAddress);
        appendResultsText("IP address2: " + ARPInfo.getIPAddressFromMAC(macAddress));

        // Send Wake on lan packed to ip/mac
        try {
            WakeOnLan.sendWakeOnLan(ipAddress, macAddress);
            appendResultsText("WOL Packet sent");
        } catch (IOException e) {
            appendResultsText(e.getMessage());
            e.printStackTrace();
        } finally {
            setEnabled(wolButton, true);
        }
    }

    private void doPortScan() throws Exception {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            setEnabled(portScanButton, true);
            return;
        }

        setEnabled(portScanButton, false);

        // Perform synchronous port scan
        appendResultsText("PortScanning IP: " + ipAddress);
        ArrayList<Integer> openPorts = PortScan.onAddress(ipAddress).setPort(21).setMethodTCP().doScan();

        final long startTimeMillis = System.currentTimeMillis();

        // Perform an asynchronous port scan
        PortScan portScan = PortScan.onAddress(ipAddress).setPortsAll().setMethodTCP().doScan(new PortScan.PortListener() {
            @Override
            public void onResult(int portNo, boolean open) {
                if (open) appendResultsText("Open: " + portNo);
            }

            @Override
            public void onFinished(ArrayList<Integer> openPorts) {
                appendResultsText("Open Ports: " + openPorts.size());
                appendResultsText("Time Taken: " + ((System.currentTimeMillis() - startTimeMillis)/1000.0f));
                setEnabled(portScanButton, true);
            }
        });

        // Below is example of how to cancel a running scan
        // portScan.cancel();
    }


    private void findSubnetDevices() {

        setEnabled(subnetDevicesButton, false);

        final long startTimeMillis = System.currentTimeMillis();

        SubnetDevices subnetDevices = SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
            @Override
            public void onDeviceFound(Device device) {
                appendResultsText("Device: " + device.ip+" "+ device.hostname);
            }

            @Override
            public void onFinished(ArrayList<Device> devicesFound) {
                float timeTaken =  (System.currentTimeMillis() - startTimeMillis)/1000.0f;
                appendResultsText("Devices Found: " + devicesFound.size());
                appendResultsText("Finished "+timeTaken+" s");
                setEnabled(subnetDevicesButton, true);
            }
        });

        // Below is example of how to cancel a running scan
        // subnetDevices.cancel();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_github) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.github_url)));
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
