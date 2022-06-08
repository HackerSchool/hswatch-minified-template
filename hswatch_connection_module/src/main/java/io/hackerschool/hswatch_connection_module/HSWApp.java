package io.hackerschool.hswatch_connection_module;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import io.hackerschool.hswatch_connection_module.flags.HSWFlag;

public class HSWApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        // Notification Channel
        NotificationChannel hswNotificationChannel = new NotificationChannel(
                HSWFlag.HSW_NOTIFICATION_CHANNEL,
                "HSWatch Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(hswNotificationChannel);
    }
}
