package co.subk.zoomsdk.meeting.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import co.subk.zoomsdk.R;
import co.subk.zoomsdk.meeting.IntegrationActivity;


public class NotificationMgr {

    public static final int PT_NOTIFICATION_ID = 4;

    public static final String ZOOM_NOTIFICATION_CHANNEL_ID = "Video_sdk_notification_channel_id";

    private NotificationMgr() {}

    public static boolean hasNotification(Context context, int notificationId) {
        try {
            NotificationManager notificationMgr = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationMgr != null) {
                StatusBarNotification[] statusBarNotifications = notificationMgr.getActiveNotifications();
                for (StatusBarNotification notification : statusBarNotifications) {
                    if (notification.getId() == notificationId) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static Notification getConfNotification(Context context) {
        Intent clickIntent = new Intent(context, IntegrationActivity.class);
        clickIntent.setAction(IntegrationActivity.ACTION_RETURN_TO_CONF);
        int flags=PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= 31){
            flags = flags | PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, clickIntent, flags);

        String contentTitle = context.getString(R.string.app_name);
        String contentText = "Meeting in progress";
        int smallIcon = R.mipmap.ic_launcher;
        int color = context.getResources().getColor(R.color.zm_notification_icon_bg);


        NotificationCompat.Builder builder = NotificationMgr.getNotificationCompatBuilder(context.getApplicationContext(), false);

        builder.setWhen(0)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(smallIcon)
                .setColor(color)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(largeIcon);
        return builder.build();
    }

    public static void removeConfNotification(Context context) {
        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationMgr.cancel(NotificationMgr.PT_NOTIFICATION_ID);
    }


    public static NotificationCompat.Builder getNotificationCompatBuilder(Context context, boolean isHighImportance) {
        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationMgr = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = notificationMgr.getNotificationChannel(getNotificationChannelId());
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(getNotificationChannelId(),
                        "Zoom Notification",
                        isHighImportance ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableVibration(true);
                if (notificationChannel.canShowBadge())
                    notificationChannel.setShowBadge(false);
            }

            notificationMgr.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(context, getNotificationChannelId());
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    public static String getNotificationChannelId() {
        return ZOOM_NOTIFICATION_CHANNEL_ID;
    }
}
