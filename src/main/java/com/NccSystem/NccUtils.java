package com.NccSystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class NccUtils {
    public static Long ip2long(String stringIp) throws UnknownHostException {
        long result = 0;

        String[] ipAddressInArray = stringIp.split("\\.");

        for (int i = 3; i >= 0; i--) {

            long ip = Long.parseLong(ipAddressInArray[3 - i]);
            result |= ip << (i * 8);
        }

        return result;
    }

    public static String long2ip(long longIp) throws UnknownHostException {
        ByteBuffer bb = ByteBuffer.allocate(4).putInt((int) longIp);
        InetAddress address = InetAddress.getByAddress(bb.array());
        return address.getHostAddress();
    }
}
