package com.subk.testing.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import co.subk.zoomsdk.event.LocationEvent;
import co.subk.zoomsdk.event.SessionEndedEvent;
import co.subk.zoomsdk.event.SessionJoinedEvent;
import co.subk.zoomsdk.event.ShareScreenEvent;
import co.subk.zoomsdk.meeting.notification.NotificationMgr;

public class EventManagementService extends Service {
    private static final String TAG = EventManagementService.class.getName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        stopMeetingService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public void onSessionStarted(SessionJoinedEvent sessionJoinedEvent) {
        Toast.makeText(this, "SessionJoinedEvent received with sessionId : " + sessionJoinedEvent.sessionId, Toast.LENGTH_SHORT).show();
//        startMeetingService();
    }

    @Subscribe
    public void onSessionEnded(SessionEndedEvent sessionEndedEvent) {
        Toast.makeText(this, "SessionEndedEvent received", Toast.LENGTH_SHORT).show();
//        stopMeetingService();
    }

    @Subscribe
    public void onShareLocationEventReceived(LocationEvent locationEvent) {
        Toast.makeText(this, "Location updated : " + locationEvent.latitude + ", " + locationEvent.longitude + " with " + locationEvent.accuracy + " accuracy ", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onShareScreenEventReceived(ShareScreenEvent shareScreenEvent) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            boolean hasForegroundNotification = NotificationMgr.hasNotification(NotificationMgr.PT_NOTICICATION_ID);
//            if (!hasForegroundNotification) {
//                Intent intent = new Intent(this, NotificationService.class);
//                startForegroundService(intent);
//            }
//        }
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected:");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected:");
        }
    };

    private void startMeetingService() {
        Intent intent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        if (null != serviceConnection) {
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void stopMeetingService() {
        Intent intent = new Intent(this, NotificationService.class);
        stopService(intent);
        try {
            if (null != serviceConnection) {
                unbindService(serviceConnection);
            }
            serviceConnection = null;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
