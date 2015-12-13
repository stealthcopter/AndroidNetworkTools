# Andorid Network Tools ![image](./app/src/main/res/mipmap-xhdpi/ic_launcher.png)

Collection of handy networking tools for android.

* Ping
* Wake-On-Lan
* 

## General info




## Usage

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

### Wake-On-Lan
```java      
      WakeOnLan.sendWakeOnLan(ipAddress, macAddress);
```

### Misc
```java      
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

