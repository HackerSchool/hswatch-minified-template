package io.hackerschool.hswatch_connection_module.services;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.hackerschool.hswatch_connection_module.connection_objects.HSWConnection;
import io.hackerschool.hswatch_connection_module.connection_objects.IHSWNotificationFilter;
import io.hackerschool.hswatch_connection_module.flags.HSWFlag;

public class HSWNotificationListener extends NotificationListenerService {

    private final HSWConnection hswConnection = new HSWConnection();

    private final List<String> receivedNotificationsList = new ArrayList<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Map<String, IHSWNotificationFilter> notificationFilterMap = hswConnection.getNotificationFilters();
        if (notificationFilterMap != null) {
            if (notificationFilterMap.containsKey(sbn.getPackageName())) {
                if (checkRepetitiveNotification(sbn.getPackageName(), sbn.getNotification())) return;
                Intent sendNotificationIntent = new Intent(HSWFlag.HSW_NOTIFICATION_RECEIVED);
                sendNotificationIntent.putExtra(
                        HSWFlag.HSW_BROADCAST_RECEIVER_EXTRA,
                        Objects.requireNonNull(notificationFilterMap.get(sbn.getPackageName()))
                                .notificationFilter(sbn.getNotification()).toArray()
                );
                sendBroadcast(sendNotificationIntent);
            }
        }
    }

    private boolean checkRepetitiveNotification(String packageName, Notification notification) {
        String title = notification.extras.getString("android.title");
        String text = notification.extras.get("android.text").toString();
        String keyMatcher = packageName + title + text;
        if (!receivedNotificationsList.contains(keyMatcher) &&
                title != null   &&
                !text.equals("null") &&
                packageName != null
        ) {
            receivedNotificationsList.add(keyMatcher);
            return false;
        }
        return true;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        String notificationListenerString = Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners");
        //Check notifications access permission
        if (!(notificationListenerString == null || !notificationListenerString.contains(getPackageName())))
        {
            requestRebind(ComponentName.createRelative(this.getApplicationContext().getPackageName(), "ListenerNotificationTest"));
        }
    }
}
