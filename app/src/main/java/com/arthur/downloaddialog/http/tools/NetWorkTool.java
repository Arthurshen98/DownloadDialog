package com.arthur.downloaddialog.http.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetWorkTool {

    public static boolean networkCanUse(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = connectivityManager.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean checkWifiConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isAvailable())
            return true;
        else
            return false;
    }

    public static boolean checkNetworkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isAvailable() || mobile.isAvailable())
            return true;
        else
            return false;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    /**
     * 判断网络类型 wifi  3G
     *
     * @param context
     * @return
     */
    public static boolean isWifiNetwrokType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if (info != null && info.isAvailable()) {
            if (info.getTypeName().equalsIgnoreCase("wifi")) {
                return true;
            }
        }
        return false;
    }

    public static String checkNetType(Context context){
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        String type = "no_network";
        if(info == null){
            return type;
        }
        if(info.getType() == ConnectivityManager.TYPE_WIFI){
            type = "wifi";
        } else {
            switch (info.getSubtype()){
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    type = "UNKNOWN";
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    type = "GPRS";
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    type = "EDGE";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    type = "UMTS";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    type = "CDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    type = "EVDO_0";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    type = "EVDO_A";
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    type = "1xRTT";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    type = "HSDPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    type = "HSUPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    type = "HSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    type = "IDEN";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    type = "EVDO_B";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    type = "LTE";
                    break;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    type = "EHRPD";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    type = "HSPAP";
                    break;
            }
        }
        return type;
    }
}


