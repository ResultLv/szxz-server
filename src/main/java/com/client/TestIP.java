package com.client;

public class TestIP {
    public static boolean hasIpChanged(String pre, String IP) {
        String[] pre_IP = pre.split("\\.");
        String[] now_IP = IP.split("\\.");
        if (!now_IP[0].equals(pre_IP[0]) || !now_IP[1].equals(pre_IP[1]) || !now_IP[2].equals(pre_IP[2])) {
            System.out.println("Changed");
            return true;
        }
        System.out.println("Unchange");
        return false;
    }

    public static void main(String[] args){
        hasIpChanged("222.92.29.18", "114.216.169.34");
    }
}
