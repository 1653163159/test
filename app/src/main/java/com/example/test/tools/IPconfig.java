package com.example.test.tools;

import java.net.*;
import java.util.*;

public class IPconfig {
    public String getIpv4() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // *EDIT*
                    if (addr instanceof Inet6Address) continue;

                    if (iface.getDisplayName().contains("VMware") || iface.getDisplayName().contains("aTrust"))
                        continue;
                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + ":" + ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

}
