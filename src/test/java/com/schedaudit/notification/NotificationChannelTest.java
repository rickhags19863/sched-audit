package com.schedaudit.notification;

import com.schedaudit.alert.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationChannelTest {

    private StubNotificationChannel channel;
    private Alert sampleAlert;

    @BeforeEach
    void setUp() {
        channel = new StubNotificationChannel("stub", true);
        sampleAlert = new Alert("backup-job", "Missed scheduled run at " + Instant.now(), Instant.now());
    }

    @Test
    void getChannelName_returnsConfiguredName() {
        assertEquals("stub", channel.getChannelName());
    }

    @Test
    void isEnabled_returnsTrueWhenEnabled() {
        assertTrue(channel.isEnabled());
    }

    @Test
    void isEnabled_returnsFalseWhenDisabled() {
        StubNotificationChannel disabled = new StubNotificationChannel("stub", false);
        assertFalse(disabled.isEnabled());
    }

    @Test
    void send_deliversAlertToChannel() throws NotificationException {
        channel.send(sampleAlert);
        assertEquals(1, channel.getSentAlerts().size());
        assertEquals(sampleAlert, channel.getSentAlerts().get(0));
    }

    @Test
    void send_multipleAlerts_allDelivered() throws NotificationException {
        Alert second = new Alert("report-job", "Missed run", Instant.now());
        channel.send(sampleAlert);
        channel.send(second);
        assertEquals(2, channel.getSentAlerts().size());
    }

    @Test
    void send_whenDisabled_throwsNotificationException() {
        StubNotificationChannel disabled = new StubNotificationChannel("stub", false);
        assertThrows(NotificationException.class, () -> disabled.send(sampleAlert));
    }

    /** Minimal in-test stub implementation of NotificationChannel. */
    private static class StubNotificationChannel implements NotificationChannel {
        private final String name;
        private final boolean enabled;
        private final List<Alert> sentAlerts = new ArrayList<>();

        StubNotificationChannel(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }

        @Override
        public String getChannelName() { return name; }

        @Override
        public void send(Alert alert) throws NotificationException {
            if (!enabled) {
                throw new NotificationException("Channel '" + name + "' is disabled");
            }
            sentAlerts.add(alert);
        }

        @Override
        public boolean isEnabled() { return enabled; }

        List<Alert> getSentAlerts() { return sentAlerts; }
    }
}
