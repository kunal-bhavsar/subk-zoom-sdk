package co.subk.zoomsdk.meeting.notification;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import us.zoom.sdk.ZoomVideoSDK;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";

    private final IBinder binder = new LocalBinder();
    public static final String ARG_COMMAND_TYPE = "args_command_type";
    public static final String ARGS_EXTRA = "args_extra";
    public static final int COMMAND_MEDIA_PROJECTION_START = 0;

    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startServiceAsForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceAsForeground();

        if (isAtLeastU()) {
            Bundle args = intent.getBundleExtra(ARGS_EXTRA);
            if (args != null) {
                int commandType = args.getInt(ARG_COMMAND_TYPE);
                if (commandType == COMMAND_MEDIA_PROJECTION_START) {
                    doMediaProjection();
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void startServiceAsForeground() {
        try {
            Notification notification = NotificationMgr.getConfNotification(getApplicationContext());
            if (isAtLeastU()) {
                startForeground(NotificationMgr.PT_NOTIFICATION_ID, notification, getCurrentForegroundServiceType());
            } else {
                startForeground(NotificationMgr.PT_NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            stopSelf();
        }
    }

    private void doMediaProjection() {
        try {
            if (isAtLeastU()) {
                int foregroundServiceType = getCurrentForegroundServiceType();
                foregroundServiceType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
                Notification notification = NotificationMgr.getConfNotification(getApplicationContext());
                startForeground(NotificationMgr.PT_NOTIFICATION_ID, notification, foregroundServiceType);
            }
        } catch (Exception e) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            NotificationMgr.removeConfNotification(getApplicationContext());
            ZoomVideoSDK.getInstance().getShareHelper().stopShare();
            ZoomVideoSDK.getInstance().leaveSession(false);
        } finally {
            stopSelf();
        }
    }

    @ChecksSdkIntAtLeast(api = 34)
    private static boolean isAtLeastU() {
        return Build.VERSION.SDK_INT >= 34;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int getCurrentForegroundServiceType() {
        int foregroundServiceType = getForegroundServiceType() | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
        if (ZoomVideoSDK.getInstance().getShareHelper().isScreenSharingOut()) {
            foregroundServiceType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
        } else {
            foregroundServiceType &= ~ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
        }
        if (isAtLeastU()) {
            if (hasPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)) {
                foregroundServiceType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            } else {
                foregroundServiceType &= ~ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            }
            if (hasPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)) {
                foregroundServiceType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
            } else {
                foregroundServiceType &= ~ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
            }
        }
        return foregroundServiceType;
    }

    public static boolean hasPermission(@Nullable Context context, @NonNull String permission) {
        if (context == null) {
            return false;
        }
        try {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.e(TAG, "Permission check failed for " + permission, e);
            return false;
        }
    }
}
