package co.subk.zoomsdk.meeting.notification;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import us.zoom.sdk.ZoomVideoSDK;


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
                Toast.makeText(NotificationService.this, "Entered in Handler", Toast.LENGTH_SHORT).show();
                Notification notification = NotificationMgr.getConfNotification();
                if (null != notification) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(NotificationMgr.PT_NOTICICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                    }
                } else {
                    stopSelf();
                }
            }
        }, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service onDestroy isInSession=:" + ZoomVideoSDK.getInstance().isInSession());
//        ZoomVideoSDK.getInstance().getShareHelper().stopShare();
//        ZoomVideoSDK.getInstance().leaveSession(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "service onTaskRemoved:"+rootIntent);
        NotificationMgr.removeConfNotification();
        stopSelf();
        ZoomVideoSDK.getInstance().getShareHelper().stopShare();
        ZoomVideoSDK.getInstance().leaveSession(false);
    }

}
