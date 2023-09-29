package co.subk.zoomsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

import co.subk.zoomsdk.event.InternetEvent;
import co.subk.zoomsdk.meeting.util.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtil.isOnline(context)) {
            EventBus.getDefault().post(new InternetEvent(true));
        } else {
            EventBus.getDefault().post(new InternetEvent(false));
        }
    }
}