package io.hackerschool.hswatch_connection_module.connection_objects;

import android.app.Notification;

import java.util.List;

public interface IHSWNotificationFilter {
    List<String> notificationFilter(Notification notification);
}
