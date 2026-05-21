package com.schedaudit.notification;

import com.schedaudit.alert.Alert;

/**
 * Defines a contract for sending alerts through various notification channels
 * (e.g., email, Slack, webhook).
 */
public interface NotificationChannel {

    /**
     * Returns the unique name identifying this channel (e.g., "email", "slack").
     */
    String getChannelName();

    /**
     * Sends the given alert through this channel.
     *
     * @param alert the alert to send
     * @throws NotificationException if delivery fails
     */
    void send(Alert alert) throws NotificationException;

    /**
     * Returns true if this channel is currently enabled and configured.
     */
    boolean isEnabled();
}
