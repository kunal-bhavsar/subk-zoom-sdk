package co.subk.zoomsdk.meeting.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkUtil {

    public static boolean hasDataNetwork(Context context) {
        if (context == null)
            return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;

        NetworkInfo ni = null;
        try {
            ni = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            return false;
        }

        if (ni == null)
            return false;

        boolean connected = ni.isConnected();

        if (connected && ni.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telMgr == null)
                return false;

            connected = (telMgr.getDataState() == TelephonyManager.DATA_CONNECTED);
        }

        return connected;
    }

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (networkInfo != null && networkInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
