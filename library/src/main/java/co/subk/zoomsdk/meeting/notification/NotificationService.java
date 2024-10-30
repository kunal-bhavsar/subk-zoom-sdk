package co.subk.zoomsdk.meeting.notification;

import android.Manifest;
import android.app.ActivityManager;
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

import java.util.List;

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
        if (intent != null && isAtLeastU()) {
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
        if (!isAppInForeground()) {
            // If the app is not in the foreground, use an alternative approach (e.g., WorkManager)

            Log.e(TAG, "Cannot start foreground service because the app is not in the foreground");
            return;
        }
        try {
            Notification notification = NotificationMgr.getConfNotification(getApplicationContext());

            if (isAtLeastU()) {
                int foregroundServiceType = getCurrentForegroundServiceType();
                startForeground(NotificationMgr.PT_NOTIFICATION_ID, notification, foregroundServiceType);
            } else {
                startForeground(NotificationMgr.PT_NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start service as foreground", e);
            stopSelf();
        }
    }

    private void doMediaProjection() {
        try {
            if (isAtLeastU()) {
                int foregroundServiceType = getCurrentForegroundServiceType() | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
                Notification notification = NotificationMgr.getConfNotification(getApplicationContext());
                startForeground(NotificationMgr.PT_NOTIFICATION_ID, notification, foregroundServiceType);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start media projection", e);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationMgr.removeConfNotification(getApplicationContext());
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

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses != null) {
                for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                    if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
