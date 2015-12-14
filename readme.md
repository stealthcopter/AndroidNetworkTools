# Andorid Network Tools ![image](./app/src/main/res/mipmap-xhdpi/ic_launcher.png)

Collection of handy networking tools for android.

* Ping
* Port Scanning
* Wake-On-Lan
* & More :)

## General info


## Usage

Requires internet permission
```xml
  <uses-permission android:name="android.permission.INTERNET" />
```

### Ping
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

### Port Scanning

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

### Wake-On-Lan

```java
      String ipAddress = "192.168.0.1";
      String macAddress = "01:23:45:67:89:ab";
      WakeOnLan.sendWakeOnLan(ipAddress, macAddress);
```

### Misc
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

