// Copyright 2020 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices;

import android.app.Notification;
import android.app.Notification.TvExtender;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.android.tv.btservices.settings.RemoteDfuConfirmationActivity;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Singleton that manages all notifications posted by BluetoothDeviceService.
 *
 * <p>This singleton class provides static methods for other components within
 * this service to post Android system notifications. This currently includes
 * low battery notification, device connect/disconnect notification, etc.
 */
public class NotificationCenter {
    private static final String TAG = "Atv.BtServices.NotificationCenter";

    private static final long REMOTE_UPDATE_SNOOZE_PERIOD =
            TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS);
    private static final String DFU_NOTIFICATION_CHANNEL = "btservices-remote-dfu-channel";
    private static final String DEFAULT_NOTIFICATION_CHANNEL = "btservices-default-channel";
    private static final String HIGH_PRIORITY_NOTIFICATION_CHANNEL = "btservices-high-channel";
    private static final String CRITICAL_NOTIFICATION_CHANNEL = "btservices-critical-channel";

    private static final long CRITICAL_BATT_CYCLE_MS = TimeUnit.MINUTES.toMillis(5);
    private static final int NOTIFICATION_RESET_HOUR_OF_DAY = 3;

    private static NotificationCenter INSTANCE;

    private static NotificationCenter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NotificationCenter();
        }

        return INSTANCE;
    }

    /**
     * Represents different battery state for battery notification.
     *
     * <p>{@code GOOD} represents good battery level that does not require
     * notification; {@code LOW} represents low battery level, user will be
     * notified to change battery soon; {@code CRITICAL} represents that battery
     * is so low that the device has disconnected and is no longer functional.
     */
    public enum BatteryState {
        GOOD,
        LOW,
        CRITICAL,
        DEPLETED,
    }

    public static synchronized void initialize(Context context) {
        getInstance().context = context;
        createNotificationChannel(context);
    }

    public static synchronized void refreshLowBatteryNotification(
            BluetoothDevice device, BatteryState state, boolean forceNotification) {
        getInstance().refreshLowBatteryNotificationImpl(device, state, forceNotification);
    }

    public static synchronized void sendDfuNotification(BluetoothDevice device) {
        getInstance().sendDfuNotificationImpl(device);
    }

    public static synchronized void dismissUpdateNotification(BluetoothDevice device) {
        getInstance().dismissUpdateNotificationImpl(device);
    }

    public static synchronized void resetUpdateNotification() {
        getInstance().dfuNotificationSnoozeWatch.clear();
    }

    private static void createNotificationChannel(Context context) {
        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        // Create notification channel for firmware update notification
        if (notificationManager.getNotificationChannel(DFU_NOTIFICATION_CHANNEL) == null) {
            CharSequence name = context.getString(R.string.settings_notif_update_channel_name);
            String description = context.getString(
                    R.string.settings_notif_update_channel_description);
            int importance = NotificationManager.IMPORTANCE_MAX;

            NotificationChannel channel =
                    new NotificationChannel(DFU_NOTIFICATION_CHANNEL, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        // Create notification channels with different priority for LauncherX
        {
            CharSequence name = context.getString(R.string.settings_notif_battery_channel_name);
            String description = context.getString(
                    R.string.settings_notif_battery_channel_description);

            if (notificationManager.getNotificationChannel(DEFAULT_NOTIFICATION_CHANNEL) == null) {
                int importance = NotificationManager.IMPORTANCE_LOW;

                NotificationChannel channel =
                    new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, name, importance);
                channel.setDescription(description);
                notificationManager.createNotificationChannel(channel);
            }

            if (notificationManager.getNotificationChannel(
                        HIGH_PRIORITY_NOTIFICATION_CHANNEL) == null) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel channel =
                    new NotificationChannel(HIGH_PRIORITY_NOTIFICATION_CHANNEL, name, importance);
                channel.setDescription(description);
                notificationManager.createNotificationChannel(channel);
            }

            if (notificationManager.getNotificationChannel(CRITICAL_NOTIFICATION_CHANNEL) == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel =
                    new NotificationChannel(CRITICAL_NOTIFICATION_CHANNEL, name, importance);
                channel.setDescription(description);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Context context;

    private final Map<BluetoothDevice, Integer> dfuNotifications = new HashMap<>();
    private final Map<BluetoothDevice, Integer> lowBatteryNotifications = new HashMap<>();
    private final Map<BluetoothDevice, Integer> criticalBatteryNotifications = new HashMap<>();
    private final Map<BluetoothDevice, Integer> depletedBatteryNotifications = new HashMap<>();
    private final Map<BluetoothDevice, Stopwatch> dfuNotificationSnoozeWatch = new HashMap<>();
    private int notificationIdCounter = 0;
    private ZonedDateTime lastNotificationTime =
            Instant.ofEpochSecond(0).atZone(ZoneId.systemDefault());

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Ticker ticker = new Ticker() {
        public long read() {
            return android.os.SystemClock.elapsedRealtimeNanos();
        }
    };

    private NotificationCenter() {}

    private void sendDfuNotificationImpl(BluetoothDevice device) {
        if (device == null) {
            Log.w(TAG, "sendDfuNotification: No Bluetooth device found for address: " + device);
            return;
        }

        Stopwatch stopwatch = dfuNotificationSnoozeWatch.get(device);
        if (stopwatch != null &&
                stopwatch.elapsed(TimeUnit.MILLISECONDS) < REMOTE_UPDATE_SNOOZE_PERIOD) {
            return;
        }

        if (stopwatch == null) {
            stopwatch = Stopwatch.createStarted(ticker);
            dfuNotificationSnoozeWatch.put(device, stopwatch);
        }

        stopwatch.reset();
        stopwatch.start();

        int notificationId;
        if (dfuNotifications.get(device) != null) {
            notificationId = dfuNotifications.get(device);
        } else {
            notificationId = notificationIdCounter++;
            dfuNotifications.put(device, notificationId);
        }

        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        final String name = BluetoothUtils.getName(device);
        Intent intent = new Intent(context, RemoteDfuConfirmationActivity.class);
        intent.putExtra(RemoteDfuConfirmationActivity.EXTRA_BT_ADDRESS, device.getAddress());
        intent.putExtra(RemoteDfuConfirmationActivity.EXTRA_BT_NAME, name);

        PendingIntent updateIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action updateAction = new Notification.Action.Builder(null,
                context.getString(R.string.settings_notif_update_action), updateIntent).build();

        Notification.Action dismissAction = new Notification.Action.Builder(null,
                context.getString(R.string.settings_notif_update_dismiss), null)
                .setSemanticAction(Notification.Action.SEMANTIC_ACTION_DELETE)
                .build();

        Icon icon = Icon.createWithResource(context, R.drawable.ic_official_remote);
        Notification notification = new Notification.Builder(context, DFU_NOTIFICATION_CHANNEL)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.settings_notif_update_title))
                .setContentText(context.getString(R.string.settings_notif_update_text))
                .setContentIntent(updateIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(updateAction)
                .addAction(dismissAction)
                .extend(new TvExtender())
                .build();
        notificationManager.notify(notificationId, notification);
    }

    private void dismissUpdateNotificationImpl(BluetoothDevice device) {
        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        if (dfuNotifications.get(device) != null) {
            int notificationId = dfuNotifications.get(device);
            notificationManager.cancel(notificationId);
        }
    }

    private void refreshLowBatteryNotificationImpl(
            BluetoothDevice device,
            BatteryState state,
            boolean forceNotification) {
        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        // Dismiss outdated notifications.
        if (state != BatteryState.LOW) {
            if (lowBatteryNotifications.get(device) != null) {
                int notificationId = lowBatteryNotifications.remove(device);
                notificationManager.cancel(notificationId);
            }
        }

        if (state != BatteryState.CRITICAL) {
            if (criticalBatteryNotifications.get(device) != null) {
                int notificationId = criticalBatteryNotifications.remove(device);
                notificationManager.cancel(notificationId);
            }
        }

        switch (state) {
            case GOOD:
                // do nothing
                break;

            case LOW:
                postLowBatteryNotification(device, forceNotification);
                break;

            case CRITICAL:
                postCriticalBatteryNotification(device, forceNotification);
                break;

            case DEPLETED:
                postDepletedBatteryNotification(device);
                break;

            default:
                // impossible to reach
                throw new AssertionError();
        }
    }

    private void postLowBatteryNotification(BluetoothDevice device, boolean forced) {
        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        if ((!forced && lowBatteryNotifications.get(device) != null) || !isNotificationAllowed()) {
            return;
        }

        int notificationId = lowBatteryNotifications.getOrDefault(device, notificationIdCounter);

        if (notificationId == notificationIdCounter) {
            notificationIdCounter++;
            lowBatteryNotifications.put(device, notificationId);
        }

        Log.w(TAG, "Low battery for remote device: " + device);

        Icon icon = Icon.createWithResource(context, R.drawable.ic_official_remote);

        Notification notification = new Notification.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.settings_notif_low_battery_title))
                .setContentText(context.getString(R.string.settings_notif_low_battery_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .extend(new TvExtender())
                .build();
        notificationManager.notify(notificationId, notification);
        logLastNotificationTime();
    }

    private void postCriticalBatteryNotification(BluetoothDevice device, boolean forced) {
        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        if ((!forced && criticalBatteryNotifications.get(device) != null) ||
                !isNotificationAllowed()) {
            return;
        }

        int notificationId = criticalBatteryNotifications.getOrDefault(device, notificationIdCounter);

        if (notificationId == notificationIdCounter) {
            notificationIdCounter++;
            criticalBatteryNotifications.put(device, notificationId);
        }

        Log.w(TAG, "Critical battery for remote device: " + device);
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);

        Icon icon = Icon.createWithResource(context, R.drawable.ic_official_remote);

        Notification notification =
                new Notification.Builder(context, HIGH_PRIORITY_NOTIFICATION_CHANNEL)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.settings_notif_critical_battery_title))
                .setContentText(context.getString(R.string.settings_notif_critical_battery_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .extend(new TvExtender())
                .build();
        notificationManager.notify(notificationId, notification);
        logLastNotificationTime();
    }

    private void postDepletedBatteryNotification(BluetoothDevice device) {
        final NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        if (depletedBatteryNotifications.get(device) != null || !isNotificationAllowed()) {
            return;
        }

        int notificationId = notificationIdCounter++;
        depletedBatteryNotifications.put(device, notificationId);

        Log.w(TAG, "Depleted battery for remote device: " + device);
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);

        Icon icon = Icon.createWithResource(context, R.drawable.ic_official_remote);

        Notification notification =
                new Notification.Builder(context, HIGH_PRIORITY_NOTIFICATION_CHANNEL)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.settings_notif_depleted_battery_title))
                .setContentText(context.getString(R.string.settings_notif_depleted_battery_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .extend(new TvExtender())
                .build();
        notificationManager.notify(notificationId, notification);
        logLastNotificationTime();
    }

    private void logLastNotificationTime() {
        lastNotificationTime = Instant.now().atZone(ZoneId.systemDefault());
    }

    private boolean isNotificationAllowed() {
        final ZonedDateTime currentTime = Instant.now().atZone(ZoneId.systemDefault());
        final ZonedDateTime resetTime =
            lastNotificationTime.plusDays(1).withHour(NOTIFICATION_RESET_HOUR_OF_DAY).withMinute(0);

        // return true if it has passed notification reset time
        return resetTime.until(currentTime, ChronoUnit.MILLIS) > 0;
    }
}
