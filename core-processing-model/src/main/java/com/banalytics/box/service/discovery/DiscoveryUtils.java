package com.banalytics.box.service.discovery;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DiscoveryUtils {

    public static String getMacByHost(String host) throws IOException {
        InetAddress address = InetAddress.getByName(host);
        String ip = address.getHostAddress();


        Process p = Runtime.getRuntime().exec("arp -a " + ip);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = input.readLine()) != null) {
            if (!line.trim().equals("")) {
                // keep only the process name
                line = line.substring(1);
                String mac = extractMacAddr(line);
                if (!mac.isEmpty()) {
                    return mac;
                }
            }
        }

        return null;
    }

    public static List<Pair<String, String>> getArpMacIpMap() throws Exception {
        List<Pair<String, String>> result = new ArrayList<>();

        Process p = Runtime.getRuntime().exec("arp -a");
        p.waitFor();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = input.readLine()) != null) {
            if (!line.trim().equals("")) {
                // keep only the process name
                line = line.substring(1);
                String ip = extractIpAddr(line);
                String mac = extractMacAddr(line);
                if (StringUtils.isNotEmpty(mac)) {
                    result.add(new ImmutablePair<>(mac, ip));
                }
            }
        }

        return result;
    }

    public static Map<String, String> getArpIpMacMap() throws IOException {
        Map<String, String> result = new HashMap<>();

        Process p = Runtime.getRuntime().exec("arp -a");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = input.readLine()) != null) {
            if (!line.trim().equals("")) {
                // keep only the process name
                line = line.substring(1);
                String ip = extractIpAddr(line);
                String mac = extractMacAddr(line);
                if (StringUtils.isNotEmpty(mac)) {
                    result.put(ip, mac);
                }
            }
        }

        return result;
    }


    public static Set<String> pingMyNetwork(String addr, String mask) throws Exception {
        Set<String> reachableIps = Collections.synchronizedSet(new HashSet<>());
        final ThreadPoolExecutor pingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        try {
            List<String> ipToPing = DiscoveryUtils.ipToScan(addr, mask);

            for (String ip : ipToPing) {
                pingExecutor.execute(() -> {
                    try {
                        InetAddress address = InetAddress.getByName(ip);
                        if (address.isReachable(1000)) {
                            reachableIps.add(ip);
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
            while (pingExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.info("Waiting ping result");
            }
            return reachableIps;
        } finally {
            pingExecutor.shutdown();
        }
    }

    private static final Pattern macPattern = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");

    private static String extractMacAddr(String str) {

        Matcher m = macPattern.matcher(str);
        if (m.find()) {
            String mac = m.group();
            mac = mac.replaceAll(":", "-").toLowerCase(Locale.ROOT);
            return mac;
        }
        return "";
    }

    private static final Pattern ipPattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");

    private static String extractIpAddr(String str) {
        Matcher m = ipPattern.matcher(str);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    public static List<String> ipToScan(String ip, String mask) throws Exception {
        List<String> result = new ArrayList<>();
        SubnetUtils su = new SubnetUtils(ip, mask);
        String[] addrs = su.getInfo().getAllAddresses();
        CollectionUtils.addAll(result, addrs);
        return result;
    }

    public static boolean available(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }


    public static String getIpAddress(byte[] rawBytes) {
        int i = 4;
        StringBuilder ipAddress = new StringBuilder();
        for (byte raw : rawBytes) {
            ipAddress.append(raw & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }

    public static List<NetworkDetails> availableSubnets() throws Exception {
        List<NetworkDetails> subnets = new ArrayList<>();
        NetworkInterface.getNetworkInterfaces().asIterator()
                .forEachRemaining(ni -> {
                    ni.getInterfaceAddresses().forEach(binding -> {
                        InetAddress addr = binding.getAddress();
                        if (addr instanceof Inet4Address) {
                            Inet4Address addr4 = (Inet4Address) addr;
                            String hostAddress = addr4.getHostAddress();
                            if ("127.0.0.1".equals(hostAddress)) {
                                return;
                            }
                            short maskLength = binding.getNetworkPrefixLength();
                            int mask = 0x80000000 >> (maskLength - 1);
                            byte[] maskArr = ByteBuffer.allocate(4).putInt(mask).array();

                            String maskStr = getIpAddress(maskArr);
                            subnets.add(new NetworkDetails(ni.getName(), ni.getDisplayName(), hostAddress, maskStr));
                        }
                    });
                });
        return subnets;
    }

    public record NetworkDetails(String name, String displayName, String address, String mask) {
    }
}
