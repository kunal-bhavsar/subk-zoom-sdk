package com.subk.testing.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import co.subk.zoomsdk.event.LocationEvent;
import co.subk.zoomsdk.event.SessionEndedEvent;
import co.subk.zoomsdk.event.SessionJoinedEvent;
import co.subk.zoomsdk.event.ShareScreenEvent;

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
    }

    @Subscribe
    public void onSessionEnded(SessionEndedEvent sessionEndedEvent) {
        Toast.makeText(this, "SessionEndedEvent received", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onShareLocationEventReceived(LocationEvent locationEvent) {
        Toast.makeText(this, "Location updated : " + locationEvent.latitude + ", " + locationEvent.longitude + " with " + locationEvent.accuracy + " accuracy ", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onShareScreenEventReceived(ShareScreenEvent shareScreenEvent) {
        Toast.makeText(this, "Screen sharing started", Toast.LENGTH_LONG).show();
    }
}