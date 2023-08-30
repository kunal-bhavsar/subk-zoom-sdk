package com.subk.testing;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import co.subk.zoomsdk.ZoomSdkHelper;
import co.subk.zoomsdk.meeting.notification.NotificationMgr;

public class NotificationService extends Service {
    private static final String TAG="NotificationService";

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }



    @Override
    public void onCreate() {
        super.onCreate();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(NotificationService.this, "Entered in Hnadler", Toast.LENGTH_SHORT).show();
                /*Notification notification = NotificationMgr.getConfNotification();
                if (null != notification) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(NotificationMgr.PT_NOTICICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                    }
                } else {
                    stopSelf();
                }*/
            }
        }, 1000);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //Notification notification = ScreenRecorderHelper.GetMediaProjectionNotification(AppShared.gContext);
            Notification notification = NotificationMgr.getConfNotification();
            startForeground(startId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }*/

        createNotificationChannel(startId);


       // handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void createNotificationChannel(int startId) {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                //.setContentTitle("SMI InstantView")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("is running......")
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        //startForeground(110, notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(startId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service onDestroy isInSession=:" + ZoomSdkHelper.isInSession());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "service onTaskRemoved:"+rootIntent);
        NotificationMgr.removeConfNotification();
        stopSelf();
        ZoomSdkHelper.stopShare();
        ZoomSdkHelper.leaveSession(false);
    }

}
