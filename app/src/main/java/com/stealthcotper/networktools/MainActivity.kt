package com.stealthcotper.networktools

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import com.stealthcopter.networktools.*
import com.stealthcopter.networktools.Ping.PingListener
import com.stealthcopter.networktools.PortScan.PortListener
import com.stealthcopter.networktools.SubnetDevices.OnSubnetDeviceFound
import com.stealthcopter.networktools.ping.PingResult
import com.stealthcopter.networktools.ping.PingStats
import com.stealthcopter.networktools.subnet.Device
import java.io.IOException
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {
    private var resultText: TextView? = null
    private var editIpAddress: EditText? = null
    private var scrollView: ScrollView? = null
    private var pingButton: Button? = null
    private var wolButton: Button? = null
    private var portScanButton: Button? = null
    private var subnetDevicesButton: Button? = null
    private var clearLogButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        resultText = findViewById(R.id.resultText)
        editIpAddress = findViewById(R.id.editIpAddress)
        scrollView = findViewById(R.id.scrollView1)
        pingButton = findViewById(R.id.pingButton)
        wolButton = findViewById(R.id.wolButton)
        portScanButton = findViewById(R.id.portScanButton)
        subnetDevicesButton = findViewById(R.id.subnetDevicesButton)
        clearLogButton = findViewById(R.id.clearLogButton)


        val ipAddress = IPTools.localIPv4Address
        if (ipAddress != null) {
            editIpAddress?.setText(ipAddress.hostAddress)
        }

        clearLogButton?.setOnClickListener{
            resultText?.text=""
        }

        pingButton?.setOnClickListener {
            Thread {
                try {
                    doPing()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        wolButton?.setOnClickListener {
            Thread {
                try {
                    doWakeOnLan()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        portScanButton?.setOnClickListener {
            Thread {
                try {
                    doPortScan()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        subnetDevicesButton?.setOnClickListener {
            Thread {
                try {
                    findSubnetDevices()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun appendResultsText(text: String?) {
        runOnUiThread {
            resultText!!.append(
                """
                $text
                """.trimIndent()
            )
            scrollView!!.post { scrollView!!.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun setEnabled(view: View?, enabled: Boolean) {
        runOnUiThread {
            if (view != null) {
                view.isEnabled = enabled
            }
        }
    }

    @Throws(Exception::class)
    private fun doPing() {
        val ipAddress = editIpAddress!!.text.toString()
        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address")
            return
        }
        setEnabled(pingButton, false)

        // Perform a single synchronous ping
        var pingResult: PingResult? = null
        pingResult = try {
            Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            appendResultsText(e.message)
            setEnabled(pingButton, true)
            return
        }
        appendResultsText("Pinging Address: " + pingResult?.address?.hostAddress)
        appendResultsText("\n")
        appendResultsText("HostName: " + pingResult?.address?.hostName)
        appendResultsText("\n")
        appendResultsText(String.format("%.2f ms", pingResult?.timeTaken))


        // Perform an asynchronous ping
        Ping.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(5).doPing(object : PingListener {
            override fun onResult(pingResult: PingResult?) {
                if (pingResult?.isReachable == true) {
                    appendResultsText("\n")
                    appendResultsText(String.format("%.2f ms", pingResult?.timeTaken))
                } else {
                    appendResultsText("\n")
                    appendResultsText(getString(R.string.timeout))
                }
            }

            override fun onFinished(pingStats: PingStats?) {
                appendResultsText("\n")
                appendResultsText(
                    String.format(
                        "Pings: %d, Packets lost: %d",
                        pingStats?.noPings, pingStats?.packetsLost
                    )
                )
                appendResultsText("\n")
                appendResultsText(
                    String.format(
                        "Min/Avg/Max Time: %.2f/%.2f/%.2f ms",
                        pingStats?.minTimeTaken,
                        pingStats?.averageTimeTaken,
                        pingStats?.maxTimeTaken
                    )
                )
                setEnabled(pingButton, true)
            }

            override fun onError(e: Exception?) {
                setEnabled(pingButton, true)
            }
        })
    }

    @Throws(IllegalArgumentException::class)
    private fun doWakeOnLan() {
        val ipAddress = editIpAddress!!.text.toString()
        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address")
            return
        }
        setEnabled(wolButton, false)
        appendResultsText("IP address: $ipAddress")

        // Get mac address from IP (using arp cache)
         val macAddress = ARPInfo.getMACFromIPAddress(ipAddress)
        if (macAddress == null) {
            appendResultsText("Could not fromIPAddress MAC address, cannot send WOL packet without it.")
            setEnabled(wolButton, true)
            return
        }
        appendResultsText("MAC address: $macAddress")
        appendResultsText("IP address2: " + ARPInfo.getIPAddressFromMAC(macAddress))

        // Send Wake on lan packed to ip/mac
        try {
            WakeOnLan.sendWakeOnLan(ipAddress, macAddress)
            appendResultsText("WOL Packet sent")
        } catch (e: IOException) {
            appendResultsText(e.message)
            e.printStackTrace()
        } finally {
            setEnabled(wolButton, true)
        }
    }

    @Throws(Exception::class)
    private fun doPortScan() {
        val ipAddress = editIpAddress!!.text.toString()
        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address")
            setEnabled(portScanButton, true)
            return
        }
        setEnabled(portScanButton, false)

        // Perform synchronous port scan
        appendResultsText("PortScanning IP: $ipAddress")
        appendResultsText("\n")
        val openPorts = PortScan.onAddress(ipAddress).setPort(21).setMethodTCP().doScan()
        val startTimeMillis = System.currentTimeMillis()

        // Perform an asynchronous port scan
        val portScan = PortScan.onAddress(ipAddress).setPortsAll().setMethodTCP()
            .doScan(object : PortListener {
                override fun onResult(portNo: Int, open: Boolean) {
                    if (open) {
                        appendResultsText("Open: $portNo")
                        appendResultsText("\n")
                    }
                }

                override fun onFinished(openPorts: java.util.ArrayList<Int>?) {
                    appendResultsText("\n")
                    appendResultsText("Open Ports: " + openPorts?.size)
                    appendResultsText("\n")
                    appendResultsText("Time Taken: " + (System.currentTimeMillis() - startTimeMillis) / 1000.0f + " s")
                    appendResultsText("\n")
                    setEnabled(portScanButton, true)
                }
            })

        // Below is example of how to cancel a running scan
        // portScan.cancel();
    }

    private fun findSubnetDevices() {
        setEnabled(subnetDevicesButton, false)
        val startTimeMillis = System.currentTimeMillis()
        val subnetDevices =
            SubnetDevices.fromLocalAddress().findDevices(object : OnSubnetDeviceFound {
                override fun onDeviceFound(device: Device?) {
                    appendResultsText("Device: " + device?.ip + " " + device?.hostname)
                    appendResultsText("\n")
                }

                override fun onFinished(devicesFound: ArrayList<Device>?) {
                    val timeTaken = (System.currentTimeMillis() - startTimeMillis) / 1000.0f
                    appendResultsText("Devices Found: " + devicesFound?.size)
                    appendResultsText("\n")
                    appendResultsText("Finished $timeTaken s")
                    appendResultsText("\n")
                    setEnabled(subnetDevicesButton, true)
                }
            })

        // Below is example of how to cancel a running scan
        // subnetDevices.cancel();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_github) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(getString(R.string.github_url))
            startActivity(i)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}