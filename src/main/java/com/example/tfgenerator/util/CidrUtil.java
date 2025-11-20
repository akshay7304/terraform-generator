package com.example.tfgenerator.util;

import java.util.ArrayList;
import java.util.List;

public class CidrUtil {

    /**
     * Split CIDR into equal subnets.
     *
     * Example:
     *   10.0.0.0/16 split into 2 =>
     *      10.0.0.0/17
     *      10.0.128.0/17
     */
    public static List<String> splitCidr(String cidr, int count) {

        if (cidr == null || !cidr.contains("/")) {
            throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
        }

        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }

        // Split CIDR "10.0.0.0/16"
        String[] cidrParts = cidr.split("/");
        String ipPart = cidrParts[0];
        int prefix = Integer.parseInt(cidrParts[1]);

        // Calculate new prefix length
        int additionalBits = (int) Math.ceil(Math.log(count) / Math.log(2));
        int newPrefix = prefix + additionalBits;

        if (newPrefix > 32) {
            throw new IllegalArgumentException("Cannot split CIDR into so many subnets.");
        }

        // Convert IP to a 32-bit integer
        long baseIp = ipv4ToLong(ipPart);

        // Subnet size
        long blockSize = 1L << (32 - newPrefix);

        List<String> subnets = new ArrayList<String>();

        // Generate each subnet
        for (int i = 0; i < count; i++) {
            long subnetIp = baseIp + (i * blockSize);
            String result = longToIpv4(subnetIp) + "/" + newPrefix;
            subnets.add(result);
        }

        return subnets;
    }

    // Convert IPv4 string → long
    private static long ipv4ToLong(String ip) {
        String[] parts = ip.split("\\.");
        long x = 0;
        for (int i = 0; i < 4; i++) {
            x = (x << 8) + Integer.parseInt(parts[i]);
        }
        return x;
    }

    // Convert 32-bit number → IPv4 string
    private static String longToIpv4(long ip) {
        return String.format(
                "%d.%d.%d.%d",
                (ip >> 24) & 0xFF,
                (ip >> 16) & 0xFF,
                (ip >> 8) & 0xFF,
                ip & 0xFF
        );
    }
}
