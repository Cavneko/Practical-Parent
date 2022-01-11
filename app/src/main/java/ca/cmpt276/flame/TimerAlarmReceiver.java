package ca.cmpt276.flame;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static androidx.core.app.NotificationCompat.FLAG_INSISTENT;

/**
 * TimerAlarmReceiver runs when the timer is finished and sends the user a notification
 */
public class TimerAlarmReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_TIMER_ALARM";
    public static final String EXTRA_CANCEL_NOTIFICATION = "CANCEL_NOTIFICATION";
    private static final long[] VIBRATION_PATTERN = {500, 1000, 500, 1000, 500, 1000};

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean cancelNotification = intent.getBooleanExtra(EXTRA_CANCEL_NOTIFICATION, false);

        if(cancelNotification) {
            cancelNotifications(context);
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.clock_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.practical_parent_icon))
                .setContentTitle(context.getString(R.string.timer_notification_title))
                .setContentText(context.getString(R.string.timer_notification_desc))
                .setContentIntent(getCancelNotificationPendingIntent(context))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(VIBRATION_PATTERN)
                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI);

        Notification notification = builder.build();
        notification.flags = FLAG_INSISTENT;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notification);
    }

    public static void cancelNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    public static PendingIntent getNotificationPendingIntent(Context context) {
        Intent intent = new Intent(context, TimerAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private static PendingIntent getCancelNotificationPendingIntent(Context context) {
        Intent intent = new Intent(context, TimerAlarmReceiver.class);
        intent.putExtra(EXTRA_CANCEL_NOTIFICATION, true);
        return PendingIntent.getBroadcast(context, 1, intent, 0);
    }
}
