package com.stealthcotper.networktools;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.stealthcopter.networktools.ARPInfo;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.WakeOnLan;
import com.stealthcopter.networktools.ping.PingResult;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  private TextView resultText;
  private EditText editIpAddress;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    resultText = (TextView) findViewById(R.id.resultText);
    editIpAddress = (EditText) findViewById(R.id.editIpAddress);

    findViewById(R.id.pingButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new Thread(new Runnable() {
          @Override public void run() {
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
          @Override public void run() {
              doWakeOnLan();
          }
        }).start();
      }
    });

    findViewById(R.id.portScanButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new Thread(new Runnable() {
          @Override public void run() {
            try {
              doPortScan();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }).start();
      }
    });

  }


  private void appendResultsText(final String text){
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        resultText.append(text+"\n");
      }
    });
  }

  private void doPing() throws Exception{
    String ipAddress = editIpAddress.getText().toString();

    // Perform a single synchronous ping
    PingResult pingResult = Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing();

    appendResultsText("Pinging Address: "+pingResult.getAddress().getHostAddress());
    appendResultsText("HostName: "+pingResult.getAddress().getHostName());
    appendResultsText(String.format("%.2f ms",pingResult.getTimeTaken()));

    // Perform an asynchronous ping
    Ping.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(5).doPing(new Ping.PingListener() {
      @Override
      public void onResult(PingResult pingResult) {
        appendResultsText(String.format("%.2f ms",pingResult.getTimeTaken()));
      }

      @Override
      public void onFinished() {

      }
    });

  }

  private void doWakeOnLan(){
    String ipAddress = editIpAddress.getText().toString();

    // Get mac address from IP (using arp cache)
    String macAddress = ARPInfo.getMACFromIPAddress(ipAddress);

    appendResultsText("IP address: "+ipAddress);
    appendResultsText("MAC address: "+macAddress);
    appendResultsText("IP address2: "+ARPInfo.getIPAddressFromMAC(macAddress));

    // Send Wake on lan packed to ip/mac
    try {
      WakeOnLan.sendWakeOnLan(ipAddress, macAddress);
      appendResultsText("WOL Packet sent");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void doPortScan() throws Exception{
    String ipAddress = editIpAddress.getText().toString();

    appendResultsText("PortScanning IP: "+ipAddress);
    ArrayList<Integer> openPorts = PortScan.onAddress(ipAddress).setPort(21).doScan();

    PortScan.onAddress(ipAddress).setTimeOutMillis(1000).setPortsAll().doScan(new PortScan.PortListener() {
      @Override
      public void onResult(int portNo, boolean open) {
        if (open) appendResultsText("Open: "+portNo);
      }

      @Override
      public void onFinished(ArrayList<Integer> openPorts) {
        appendResultsText("Open Ports: "+openPorts.size());
      }
    });



  }

}
