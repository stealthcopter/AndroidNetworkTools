# Andorid Network Tools ![image](./app/src/main/res/mipmap-xhdpi/ic_launcher.png)
==================

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-AndroidNetworkTools-green.svg?style=true)](https://android-arsenal.com/details/1/3112)

Disapointed by the lack of good network apis in andorid / java I developed a collection of handy networking tools for everyday andorid development.

* Ping
* Port Scanning
* Subnet tools (find devices on local network)
* Wake-On-Lan
* & More :)

## General info

### Sample app

The sample app is published on Google play to allow you to quickly and easier test the library. Enjoy! And please do feedback to us if your tests produce different results. 
 
<a href="https://play.google.com/store/apps/details?id=com.stealthcotper.networktools&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" width="150"/></a>

## Usage

### Add as dependency
This library is not yet released in Maven Central, until then you can add as a library module or use JitPack.io

add remote maven url

```groovy

    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
```
    
then add a library dependency. **Remember** to check for latest release [here](https://github.com/stealthcopter/AndroidNetworkTools/releases/) 

```groovy
    dependencies {
        compile 'com.github.stealthcopter:AndroidNetworkTools:0.1'
    }
```

### Add permission
Requires internet permission (obviously...)
```xml
  <uses-permission android:name="android.permission.INTERNET" />
```

### Ping

Uses the native ping binary if avaliable on the device (some devices come without it) and falls back to a TCP request on port 7 (echo request) if not.

```java
     // Synchronously 
     PingResult pingResult = Ping.onAddress("192.168.0.1").setTimeOutMillis(1000).doPing();
     
     // Asynchronously
     Ping.onAddress("192.168.0.1").setTimeOutMillis(1000).setTimes(5).doPing(new Ping.PingListener() {
      @Override
      public void onResult(PingResult pingResult) {
        ...
      }
    });
```

Note: If we do have to fall back to using TCP port 7 (the java way) to detect devices we will find significantly less than with the native ping binary. If this is an issue you could consider adding a ping binary to your application or device so that it is always avaliable.

### Port Scanning

A simple java based TCP port scanner, fast and easy to use. 

```java
    // Synchronously 
    ArrayList<Integer> openPorts = PortScan.onAddress("192.168.0.1").setPort(21).doScan();
    
    // Asynchronously
    PortScan.onAddress("192.168.0.1").setTimeOutMillis(1000).setPortsAll().doScan(new PortScan.PortListener() {
      @Override
      public void onResult(int portNo, boolean open) {
        if (open) // Stub: found open port
      }

      @Override
      public void onFinished(ArrayList<Integer> openPorts) {
	// Stub: finished scanning
      }
    });

```
Note: If you want a more advanced portscanner you should consider compiling nmap into your project and using that instead.

### Wake-On-Lan

Sends a Wake-on-Lan packet to the IP / MAC address

```java
      String ipAddress = "192.168.0.1";
      String macAddress = "01:23:45:67:89:ab";
      WakeOnLan.sendWakeOnLan(ipAddress, macAddress);
```

### Misc

Other useful methods:

```java
      String ipAddress = "192.168.0.1";
      String macAddress = ARPInfo.getMacFromArpCache(ipAddress);
```

## Building

It's a standard gradle project.


# Contributing

I welcome pull requests, issues and feedback.

- Fork it
- Create your feature branch (git checkout -b my-new-feature)
- Commit your changes (git commit -am 'Added some feature')
- Push to the branch (git push origin my-new-feature)
- Create new Pull Request

