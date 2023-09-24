package co.subk.zoomsdk;

import static co.subk.zoomsdk.ZoomSdkHelper.EVENT_INTERNET_CHANGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import co.subk.zoomsdk.model.InternetEvent;

public class NetworkChangeReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (isOnline(context)) {
                EventBus.getDefault().post(new InternetEvent(EVENT_INTERNET_CHANGE, true));
            } else {
                EventBus.getDefault().post(new InternetEvent(EVENT_INTERNET_CHANGE, false));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}