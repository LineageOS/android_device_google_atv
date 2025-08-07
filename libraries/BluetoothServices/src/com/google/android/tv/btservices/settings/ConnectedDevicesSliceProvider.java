/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.tv.btservices.settings;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;
import static android.media.tv.flags.Flags.hdmiControlEnhancedBehavior;
import static android.media.tv.flags.Flags.enableLeAudioUnicastUi;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_SLICE_FOLLOWUP;

import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.CONT_CANCEL_ARGS;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_CONNECT;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_DISCONNECT;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_BLE_INFO;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_FORGET;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_RENAME;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_UPDATE;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.YES_NO_ARGS;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_ACCESSORIES;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_REMOTE_SETUP_TOGGLE;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_CEC_TOGGLE;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_DEVICE_CONTROL;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_FIND_MY_REMOTE_TOGGLE;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_OFFICIAL_REMOTES;
import static com.google.android.tv.btservices.settings.ConnectedDevicesPreferenceFragment.KEY_PAIR_REMOTE;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.ACTION_BACKLIGHT;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.ACTION_FIND_MY_REMOTE;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.ACTION_TOGGLE_CHANGED;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.ACTIVE_AUDIO_OUTPUT;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.LE_AUDIO_UNICAST;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.CEC;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.POWER_STATE_CHANGE_ON_ACTIVE_SOURCE_LOST;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.TOGGLE_STATE;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.TOGGLE_TYPE;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.backAndUpdateSliceIntent;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.updateSliceIntent;
import static com.google.android.tv.btservices.settings.SliceBroadcastReceiver.getBacklightModeIntent;
import static com.google.android.tv.btservices.settings.SlicesUtil.FIND_MY_REMOTE_PHYSICAL_BUTTON_ENABLED_SETTING;
import static com.google.android.tv.btservices.settings.SlicesUtil.GENERAL_SLICE_URI;
import static com.google.android.tv.btservices.settings.SlicesUtil.isFindMyRemoteButtonEnabled;
import static com.google.android.tv.btservices.settings.SlicesUtil.BACKLIGHT_MODE_SETTING;
import static com.google.android.tv.btservices.settings.SlicesUtil.getBacklightMode;
import static com.google.android.tv.btservices.settings.DialogResponseActivity.DIALOG_ARG_ICON;
import static com.google.android.tv.btservices.settings.DialogResponseActivity.DIALOG_ARG_KEY;
import static com.google.android.tv.btservices.settings.DialogResponseActivity.DIALOG_ARG_NAME;
import static com.google.android.tv.btservices.settings.DialogResponseActivity.DIALOG_ARG_SUMMARY;
import static com.google.android.tv.btservices.settings.DialogResponseActivity.DIALOG_ARG_TITLE;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;

import com.android.settingslib.media.flags.Flags;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;

import com.google.android.tv.btservices.BluetoothDeviceService;
import com.google.android.tv.btservices.BluetoothUtils;
import com.google.android.tv.btservices.Configuration;
import com.google.android.tv.btservices.PowerUtils;
import com.google.android.tv.btservices.R;
import com.google.android.tv.btservices.SettingsUtils;
import com.google.android.tv.btservices.remote.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use slice to provide TvSettings "connected devices" information.
 */
public class ConnectedDevicesSliceProvider extends SliceProvider implements
        BluetoothDeviceProvider.Listener, ConnectedDevicesPreferenceFragment.Provider {
    private static final String TAG = "Atv.ConDevsSliceProvider";
    private static final boolean DEBUG = false;
    private static final boolean DISCONNECT_PREFERENCE_ENABLED = false;
    private static final int ACTIVE_AUDIO_OUTPUT_REQUEST_CODE = 4;
    private static final int ACTIVE_AUDIO_OUTPUT_UPDATE_REQUEST_CODE = 5;
    private static final int LE_AUDIO_UNICAST_REQUEST_CODE = 6;
    private static final int LE_AUDIO_UNICAST_UPDATE_REQUEST_CODE = 7;
    private boolean mBtDeviceServiceBound;
    private final Map<String, Version> mVersionsMap = new ConcurrentHashMap<>();
    private BluetoothDeviceService.LocalBinder mBtDeviceServiceBinder;
    private final Map<Uri, Integer> pinnedUris = new ArrayMap<>();

    static final String KEY_EXTRAS_DEVICE = "key_extras_device";
    static final String KEY_BACKLIGHT_RADIO_GROUP = "backlight_radio_group";
    static final String KEY_TOGGLE_ACTIVE_AUDIO_OUTPUT = "toggle_active_audio_output";

    private static final String KEY_EXTERNAL_SPEAKER = "external_speaker";

    private static final String SCHEME_CONTENT = "content://";
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection mBtDeviceServiceConnection =
            new com.google.android.tv.btservices.SimplifiedConnection() {
                @Override
                protected void cleanUp() {
                    if (mBtDeviceServiceBinder != null) {
                        mBtDeviceServiceBinder.removeListener(ConnectedDevicesSliceProvider.this);
                    }
                    mBtDeviceServiceBinder = null;
                }

                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    mBtDeviceServiceBinder = (BluetoothDeviceService.LocalBinder) service;
                    mBtDeviceServiceBinder.addListener(ConnectedDevicesSliceProvider.this);
                    getContext().getContentResolver().notifyChange(SlicesUtil.GENERAL_SLICE_URI,
                            null);
                }
            };

    // BluetoothDeviceProvider.Listener implementation
    @Override
    public void onDeviceUpdated(BluetoothDevice device) {
        getContext().getContentResolver().notifyChange(SlicesUtil.GENERAL_SLICE_URI, null);
        getContext().getContentResolver().notifyChange(
                SlicesUtil.getDeviceUri(device.getAddress()), null);
        updateVersionAndNotify(device);
    }

    private void updateVersionAndNotify(BluetoothDevice device) {
        String addr = device.getAddress();
        mHandler.post(() -> {
            mVersionsMap.put(addr, getLocalBluetoothDeviceProvider().getVersion(device));
            if (device != null) {
                getContext().getContentResolver().notifyChange(
                        SlicesUtil.getDeviceUri(addr), null);
            }
        });
    }

    @Override
    public boolean isCecEnabled() {
        return PowerUtils.isCecControlEnabled(getContext());
    }

    public List<BluetoothDevice> getBluetoothDevices() {
        if (mBtDeviceServiceBinder != null) {
            return mBtDeviceServiceBinder.getDevices();
        }
        return new ArrayList<>();
    }

    @Override
    public BluetoothDeviceProvider getBluetoothDeviceProvider() {
        return mBtDeviceServiceBinder;
    }

    public BluetoothDeviceProvider getLocalBluetoothDeviceProvider() {
        return mLocalBluetoothDeviceProvider;
    }

    @Override
    public void onSlicePinned(Uri sliceUri) {
        mHandler.post(() -> {
            if (DEBUG) {
                Log.d(TAG, "Slice pinned: " + sliceUri);
            }
            Context context = getContext();
            if (!mBtDeviceServiceBound && context.bindService(
                    new Intent(context, BluetoothUtils.getBluetoothDeviceServiceClass(context)),
                    mBtDeviceServiceConnection, Context.BIND_AUTO_CREATE)) {
                mBtDeviceServiceBound = true;
            }
            if (!pinnedUris.containsKey(sliceUri)) {
                pinnedUris.put(sliceUri, 0);
            }
            pinnedUris.put(sliceUri, pinnedUris.get(sliceUri) + 1);
        });
    }

    @Override
    public void onSliceUnpinned(Uri sliceUri) {
        mHandler.post(() -> {
            if (DEBUG) {
                Log.d(TAG, "Slice unpinned: " + sliceUri);
            }
            Context context = getContext();
            // If at this point there is only one slice pinned, we need to unbind the service as
            // there won't be any slice pinned after handleSliceUnpinned is called.
            if (pinnedUris.containsKey(sliceUri)) {
                int newCount = pinnedUris.get(sliceUri) - 1;
                pinnedUris.put(sliceUri, newCount);
                if (newCount == 0) {
                    pinnedUris.remove(sliceUri);
                }
            }
            if (pinnedUris.isEmpty() && mBtDeviceServiceBound) {
                context.unbindService(mBtDeviceServiceConnection);
                mBtDeviceServiceBound = false;
            }
        });
    }

    @Override
    public boolean onCreateSliceProvider() {
        return true;
    }

    private String getString(int id) {
        return getContext().getString(id);
    }

    @Override
    public Slice onBindSlice(Uri sliceUri) {
        if (DEBUG) {
            Log.d(TAG, "onBindSlice: " + sliceUri);
        }
        if (SlicesUtil.isGeneralPath(sliceUri)) {
            return createGeneralSlice(sliceUri);
        } else if (SlicesUtil.isBluetoothDevicePath(sliceUri)) {
            return createBluetoothDeviceSlice(sliceUri);
        } else if (SlicesUtil.isCecPath(sliceUri)) {
            return createCecSlice(sliceUri);
        } else if (SlicesUtil.isFindMyRemotePath(sliceUri)) {
            return createFindMyRemoteSlice(sliceUri);
        } else if (SlicesUtil.isBacklightPath(sliceUri)) {
            return createBacklightSlice(sliceUri);
        }
        return null;
    }

    @Override
    public PendingIntent onCreatePermissionRequest(Uri sliceUri, String callingPackage) {
        final Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        return PendingIntent.getActivity(getContext(), 0, settingsIntent, FLAG_IMMUTABLE);
    }

    // The initial slice in the Connected Device flow.
    private Slice createGeneralSlice(Uri sliceUri) {
        PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);
        psb.addScreenTitle(new RowBuilder()
                .setTitle(getString(R.string.connected_devices_slice_pref_title))
                .setPageId(0x18000000)); // TvSettingsEnums.CONNECTED_SLICE

        RestrictedLockUtils.EnforcedAdmin admin =
                RestrictedLockUtilsInternal.checkIfRestrictionEnforced(getContext(),
                        UserManager.DISALLOW_CONFIG_BLUETOOTH, UserHandle.myUserId());
        PendingIntent pendingIntent;
        List<String> updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString());
        PendingIntent updateGeneralSliceIntent = updateSliceIntent(getContext(), 0,
                new ArrayList<>(updatedUris), GENERAL_SLICE_URI.toString());
        if (admin == null) {
            Intent i = SettingsUtils.getPairingIntent()
                    .putExtra(EXTRA_SLICE_FOLLOWUP, updateGeneralSliceIntent);
            pendingIntent = PendingIntent.getActivity(
                    getContext(), 0, i, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
        } else {
            Intent intent = RestrictedLockUtils.getShowAdminSupportDetailsIntent(
                            getContext(), admin)
                    .putExtra(DevicePolicyManager.EXTRA_RESTRICTION,
                            UserManager.DISALLOW_CONFIG_BLUETOOTH)
                    .putExtra(EXTRA_SLICE_FOLLOWUP, updateGeneralSliceIntent);
            pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, FLAG_IMMUTABLE);
        }
        psb.addPreference(new RowBuilder()
                .setKey(KEY_PAIR_REMOTE)
                .setTitle(getString(R.string.settings_pair_remote))
                .setActionId(0x18100000) // TvSettingsEnums.CONNECTED_SLICE_CONNECT_NEW_DEVICES
                .setIcon(IconCompat.createWithResource(getContext(),
                        R.drawable.ic_baseline_add_24dp))
                .setIconNeedsToBeProcessed(true)
                .setPendingIntent(pendingIntent)
        );

        // Visually, the connected devices concept covers two categories:
        // 1. Any connected devices except the official remote controls. Actively connected devices
        //    are ranked higher.
        // 2. Official remote controls (if present) and their button settings.
        updateConnectedDevicesSlice(psb);
        updateDeviceControlSlice(psb);
        return psb.build();
    }

    private void updateConnectedDevicesSlice(PreferenceSliceBuilder psb) {
        // Overall BT devices maps
        HashMap<String, BluetoothDevice> addressToDevice = new HashMap<>();
        // Sets for BT devices that are not official remotes:
        // - activeAccessories: they are considered connected from both BluetoothDevice and
        //       CachedBluetoothDevice's perceptive.
        // - inactiveAccessories: they are considered connected from BluetoothDevice's perceptive
        //       but disconnected from CachedBluetoothDevice's perceptive. They can be easily
        //       reconnected.
        // - bondedAccessories: they are considered merely bonded but not connected from
        //       BluetoothDevice's perceptive.
        Set<String> activeAccessories = new HashSet<>();
        Set<String> inactiveAccessories = new HashSet<>();
        Set<String> bondedAccessories = new HashSet<>();
        // Sets for official BT remotes
        // - activeOfficialRemotes: they are considered connected from both BluetoothDevice and
        //       CachedBluetoothDevice's perceptive.
        // - inactiveOfficialRemotes: they are official remotes in states corresponding to the union
        //       of inactiveAccessories and bondedAccessories.
        Set<String> activeOfficialRemotes = new HashSet<>();
        Set<String> inactiveOfficialRemotes = new HashSet<>();
        boolean hasActiveDevices = false;

        // Bucketing all BT devices
        for (BluetoothDevice device : getBluetoothDevices()) {
            CachedBluetoothDevice cachedDevice =
                    BluetoothUtils.getCachedBluetoothDevice(getContext(), device);
            if (BluetoothUtils.isConnected(device)) {
                addressToDevice.put(device.getAddress(), device);
                if (cachedDevice != null && cachedDevice.isConnected()) {
                    if (BluetoothUtils.isOfficialRemote(getContext(), device)) {
                        activeOfficialRemotes.add(device.getAddress());
                    } else {
                        activeAccessories.add(device.getAddress());
                    }
                } else {
                    if (BluetoothUtils.isOfficialRemote(getContext(), device)) {
                        inactiveOfficialRemotes.add(device.getAddress());
                    } else {
                        inactiveAccessories.add(device.getAddress());
                    }
                }
            } else if (BluetoothUtils.isBonded(device)) {
                addressToDevice.put(device.getAddress(), device);
                if (BluetoothUtils.isOfficialRemote(getContext(), device)) {
                    inactiveOfficialRemotes.add(device.getAddress());
                } else {
                    bondedAccessories.add(device.getAddress());
                }
            }
        }

        // "Accessories" category
        if (activeAccessories.size() + inactiveAccessories.size() + bondedAccessories.size()
                > 0) {
            psb.addPreferenceCategory(new RowBuilder()
                    .setTitle(getContext().getString(R.string.settings_known_devices_category))
                    .setKey(KEY_ACCESSORIES));
            // Add accessories following the ranking of: active, inactive, bonded.
            createAndAddBtDeviceSlicePreferenceFromSet(psb, activeAccessories, addressToDevice);
            createAndAddBtDeviceSlicePreferenceFromSet(psb, inactiveAccessories, addressToDevice);
            createAndAddBtDeviceSlicePreferenceFromSet(psb, bondedAccessories, addressToDevice);
            hasActiveDevices = true;
        }

        // Add a section for external speakers.
        updateExternalSpeakerSlice(psb);

        // "Official remote" category
        if (activeOfficialRemotes.size() + inactiveOfficialRemotes.size() > 0) {
            psb.addPreferenceCategory(new RowBuilder()
                    .setTitle(getContext().getString(R.string.settings_official_remote_category))
                    .setKey(KEY_OFFICIAL_REMOTES));
            createAndAddBtDeviceSlicePreferenceFromSet(psb, activeOfficialRemotes, addressToDevice);
            createAndAddBtDeviceSlicePreferenceFromSet(
                    psb, inactiveOfficialRemotes, addressToDevice);
            hasActiveDevices = true;
        }

        // Adding the remote buttons settings at the bottom
        updateRemoteSetupSlice(psb);

        if (hasActiveDevices) {
            updateFindMyRemoteSlice(psb);
            // Temporarily disable the backlight settings due to b/408118325, will re-enable it
            // after the firmware is upgraded to fix the GATT issue.
            //updateBacklight(psb);
        }
    }

    private void updateDeviceControlSlice(PreferenceSliceBuilder psb) {
        // Currently, HDMI-CEC settings is the only entry within the device control category so we
        // should hide the whole category including the header if HDMI-CEC settings is not visible.
        if (!ConnectedDevicesPreferenceFragment.isCecSettingsEnabled(getContext())
                || !showCecInConnectedSettings()) {
            return;
        }
        RowBuilder category = new RowBuilder().setKey(KEY_DEVICE_CONTROL)
                .setTitle(getString(R.string.settings_devices_control));
        psb.addPreferenceCategory(category);
        updateCecSettings(psb);
    }

    private void updateExternalSpeakerSlice(PreferenceSliceBuilder psb) {
        String uri = getString(R.string.external_speaker_slice_provider_uri);
        if (TextUtils.isEmpty(uri)) {
            return;
        }

        ProviderInfo provider =
                getContext()
                        .getPackageManager()
                        .resolveContentProvider(Uri.parse(uri).getAuthority(), 0);
        if (provider == null) {
            return;
        }

        String category = getString(R.string.external_speaker_category);
        if (TextUtils.isEmpty(category)) {
            return;
        }

        psb.addPreferenceCategory(new RowBuilder().setTitle(category).setKey(KEY_EXTERNAL_SPEAKER));
        psb.addEmbeddedPreference(new RowBuilder().setTargetSliceUri(uri));
    }

    private void updateRemoteSetupSlice(PreferenceSliceBuilder psb) {
        if (!ConnectedDevicesPreferenceFragment.isAxelSettingsEnabled(getContext())) {
            if (InputDeviceKeyDetector.isCustomButtonPresent(getContext(),
                    KeyEvent.KEYCODE_MACRO_1)) {
                RowBuilder lxPref = new RowBuilder()
                    .setKey(KEY_REMOTE_SETUP_TOGGLE)
                    .setTitle(getString(R.string.settings_remote_setup))
                    .setSubtitle(getString(R.string.settings_remote_setup_description))
                    .setTargetSliceUri(SlicesUtil.LAUNCHERX_SLICE_PATH);
                psb.addPreference(lxPref);
            } else {
                return;
            }
        } else {
            RowBuilder axelPref = new RowBuilder()
                .setKey(KEY_REMOTE_SETUP_TOGGLE)
                .setTitle(getString(R.string.settings_remote_setup))
                .setSubtitle(getString(R.string.settings_remote_setup_description))
                .setTargetSliceUri(SlicesUtil.AXEL_SLICE_PATH);
            psb.addPreference(axelPref);
        }
    }

    private void updateFindMyRemoteSlice(PreferenceSliceBuilder psb) {
        List<ResolveInfo> receivers = getContext().getPackageManager().queryBroadcastReceivers(
                new Intent(ACTION_FIND_MY_REMOTE), 0);
        if (receivers.isEmpty()) {
            return;
        }

        psb.addPreference(new RowBuilder()
                .setKey(KEY_FIND_MY_REMOTE_TOGGLE)
                .setTitle(getString(R.string.settings_find_my_remote_title))
                .setSubtitle(getString(R.string.settings_find_my_remote_description))
                .setTargetSliceUri(SlicesUtil.FIND_MY_REMOTE_SLICE_URI.toString()));
    }

    private void updateBacklight(PreferenceSliceBuilder psb) {
        Context context = getContext();

        List<ResolveInfo> receivers = getContext().getPackageManager().queryBroadcastReceivers(
                new Intent(ACTION_BACKLIGHT), 0);
        if (receivers.isEmpty()) {
            return;
        }

        psb.addPreference(new RowBuilder()
                .setKey(KEY_BACKLIGHT_RADIO_GROUP)
                .setTitle(getString(R.string.settings_backlight_title))
                .setSubtitle(getString(R.string.settings_backlight_description))
                .setTargetSliceUri(SlicesUtil.BACKLIGHT_SLICE_URI.toString()));
    }

    private void updateCecSettings(PreferenceSliceBuilder psb) {
        if (!ConnectedDevicesPreferenceFragment.isCecSettingsEnabled(getContext())
                || !showCecInConnectedSettings()) {
            return;
        }
        RowBuilder cecPref = new RowBuilder()
                .setKey(KEY_CEC_TOGGLE)
                .setTitle(getString(R.string.settings_hdmi_cec))
                .setSubtitle(isCecEnabled() ? getString(R.string.settings_enabled)
                        : getString(R.string.settings_disabled))
                .setTargetSliceUri(SlicesUtil.CEC_SLICE_URI.toString());
        psb.addPreference(cecPref);
    }

    // The slice page that shows detail information of a particular device.
    private Slice createBluetoothDeviceSlice(Uri sliceUri) {
        Context context = getContext();
        String deviceAddr = SlicesUtil.getDeviceAddr(sliceUri);
        BluetoothDevice device = BluetoothDeviceService
                .findDevice(SlicesUtil.getDeviceAddr(sliceUri));
        CachedBluetoothDevice cachedDevice =
                BluetoothUtils.getCachedBluetoothDevice(getContext(), device);
        BluetoothDeviceProvider btDeviceProvider = getLocalBluetoothDeviceProvider();
        String deviceName = "";
        if (device != null) {
            deviceName = BluetoothUtils.getName(device);
        }

        PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        Bundle extras = new Bundle();
        Intent i = null;
        if (BluetoothUtils.supportBtDeviceServiceUpdate(context, device)) {
            RowBuilder updatePref = new RowBuilder().setKey(KEY_UPDATE);
            i = new Intent(context, DialogResponseActivity.class);
            i.putExtra(DIALOG_ARG_TITLE, R.string.settings_bt_update);
            i.putExtra(DIALOG_ARG_SUMMARY, R.string.settings_bt_update_summary);
            i.putExtra(DIALOG_ARG_NAME, deviceName);
            i.putExtra(DIALOG_ARG_KEY, KEY_UPDATE);
            i.putExtra(DIALOG_ARG_ICON, R.drawable.ic_info_compound);
            i.putExtra(KEY_EXTRAS_DEVICE, device);
            i.setData(Uri.parse(SCHEME_CONTENT + device.getAddress()));
            List<String> updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString(),
                    sliceUri.toString());
            PendingIntent updateSliceIntent = updateSliceIntent(getContext(), 0,
                    new ArrayList<>(updatedUris), sliceUri.toString());
            i.putExtra(EXTRA_SLICE_FOLLOWUP, updateSliceIntent);
            PendingIntent updatePendingIntent =
                    PendingIntent.getActivity(context, 0, i,
                            FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
            updatePref.setPendingIntent(updatePendingIntent);

            if (btDeviceProvider.hasUpgrade(device)) {
                updatePref.setTitle(getString(R.string.settings_bt_update));
                updatePref.setEnabled(true);
                updatePref.setSelectable(true);
                // TvSettingsEnums.CONNECTED_SLICE_DEVICE_ENTRY_UPDATE
                updatePref.setActionId(0x18210000);
                if (btDeviceProvider.isBatteryLow(device)) {
                    updatePref.setSubtitle(getString(R.string.settings_bt_battery_low));
                    updatePref.setEnabled(false);
                    updatePref.setSelectable(false);
                } else {
                    updatePref.setSubtitle(
                            getString(R.string.settings_bt_update_software_available));
                }
            } else {
                updatePref.setTitle(getString(R.string.settings_bt_update_not_necessary));
                updatePref.setSubtitle(null);
                updatePref.setEnabled(false);
                updatePref.setSelectable(false);
            }

            psb.addPreference(updatePref);
        }

        // Update "Use for TV audio".
        // Set as active audio output device only connected devices that have audio capabilities
        if (Flags.enableTvMediaOutputDialog()
                && cachedDevice != null && !cachedDevice.isBusy()
                && BluetoothUtils.isConnected(device) && cachedDevice.isConnected()
                && (BluetoothUtils.isBluetoothHeadset(device)
                || BluetoothUtils.hasAudioProfile(cachedDevice))) {
            boolean isActive = BluetoothUtils.isActiveA2dpOrLeAudioOutput(device);

            Intent intent = new Intent(ACTION_TOGGLE_CHANGED);
            intent.setClass(context, SliceBroadcastReceiver.class);
            intent.putExtra(TOGGLE_TYPE, ACTIVE_AUDIO_OUTPUT);
            intent.putExtra(TOGGLE_STATE, !isActive);
            intent.putExtra(KEY_EXTRAS_DEVICE, device);

            List<String> updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString(), sliceUri.toString());
            PendingIntent updateSliceIntent = updateSliceIntent(getContext(),
                    ACTIVE_AUDIO_OUTPUT_UPDATE_REQUEST_CODE, new ArrayList<>(updatedUris),
                    sliceUri.toString());
            intent.putExtra(EXTRA_SLICE_FOLLOWUP, updateSliceIntent);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    ACTIVE_AUDIO_OUTPUT_REQUEST_CODE, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Update set/unset active audio output preference
            RowBuilder activeAudioOutputPref = new RowBuilder()
                    .setKey(KEY_TOGGLE_ACTIVE_AUDIO_OUTPUT)
                    .setTitle(getString(R.string.bluetooth_toggle_active_audio_output))
                    .setActionId(0x18240000) // TvSettingsEnums.CONNECTED_SLICE_DEVICE_ENTRY_TOGGLE_ACTIVE_AUDIO_OUTPUT)
                    .addSwitch(pendingIntent,
                            context.getText(R.string.bluetooth_toggle_active_audio_output),
                            isActive);

            psb.addPreference(activeAudioOutputPref);
        }

        // Update "connect/disconnect preference"
        if (cachedDevice != null && !cachedDevice.isBusy()) {
            // Whether the device is actually connected from CachedBluetoothDevice's perceptive.
            boolean isConnected = BluetoothUtils.isConnected(device) && cachedDevice.isConnected();
            if (!isConnected || showDisconnectButton(device, context)) {
                RowBuilder disconnectPref = new RowBuilder()
                        .setKey(isConnected ? KEY_DISCONNECT : KEY_CONNECT)
                        .setTitle(getString(isConnected
                                ? R.string.bluetooth_disconnect : R.string.bluetooth_connect));
                extras = new Bundle();
                i = new Intent(context, DialogResponseActivity.class);
                i.putExtra(DIALOG_ARG_TITLE, isConnected ? R.string.settings_bt_disconnect : R.string.settings_bt_connect);
                i.putExtra(DIALOG_ARG_NAME, deviceName);
                i.putExtra(DIALOG_ARG_ICON, R.drawable.ic_bluetooth_compound);
                i.putExtra(DIALOG_ARG_KEY, isConnected ? KEY_DISCONNECT : KEY_CONNECT);
                i.putExtra(KEY_EXTRAS_DEVICE, device);
                i.setData(Uri.parse(SCHEME_CONTENT + device.getAddress()));
                List<String> updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString(),
                        sliceUri.toString());
                PendingIntent updateSliceIntent = backAndUpdateSliceIntent(getContext(), 1,
                        new ArrayList<>(updatedUris), sliceUri.toString());
                i.putExtra(EXTRA_SLICE_FOLLOWUP, updateSliceIntent);
                PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, 1, i,
                                FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
                disconnectPref.setPendingIntent(pendingIntent);
                psb.addPreference(disconnectPref);
            }
        }

        // Update "rename preference".
        RowBuilder renamePref = new RowBuilder()
                .setKey(KEY_RENAME)
                .setTitle(getString(R.string.bluetooth_rename))
                .setActionId(0x18220000); // TvSettingsEnums.CONNECTED_SLICE_DEVICE_ENTRY_RENAME
        extras = new Bundle();
        ResponseFragment.prepareArgs(
                extras,
                KEY_RENAME,
                R.string.settings_bt_rename,
                0,
                R.drawable.ic_baseline_bluetooth_searching_large,
                null,
                deviceName,
                ResponseFragment.DEFAULT_CHOICE_UNDEFINED
        );
        i = new Intent(context, ResponseActivity.class)
                .putExtra(KEY_EXTRAS_DEVICE, device)
                .putExtras(extras)
                .setData(Uri.parse(SCHEME_CONTENT + device.getAddress()));
        List<String> updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString(), sliceUri.toString());
        PendingIntent updateSliceIntent = updateSliceIntent(getContext(), 2,
                new ArrayList<>(updatedUris), sliceUri.toString());
        i.putExtra(EXTRA_SLICE_FOLLOWUP, updateSliceIntent);
        PendingIntent renamePendingIntent = PendingIntent
                .getActivity(context, 2, i,
                        FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
        renamePref.setPendingIntent(renamePendingIntent);
        psb.addPreference(renamePref);

        // Update "Forget Preference"
        RowBuilder forgetPref =
            new RowBuilder()
                .setKey(KEY_FORGET)
                .setTitle(getString(R.string.bluetooth_forget))
                .setActionId(0x18230000); // TvSettingsEnums.CONNECTED_SLICE_DEVICE_ENTRY_FORGET
        i = new Intent(context, DialogResponseActivity.class);
        i.putExtra(DIALOG_ARG_TITLE, R.string.settings_bt_forget);
        i.putExtra(DIALOG_ARG_NAME, deviceName);
        i.putExtra(DIALOG_ARG_ICON, R.drawable.ic_info_compound);
        i.putExtra(DIALOG_ARG_KEY, KEY_FORGET);
        i.putExtra(KEY_EXTRAS_DEVICE, device);
        i.setData(Uri.parse(SCHEME_CONTENT + device.getAddress()));
        updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString(), sliceUri.toString());
        updateSliceIntent =
            backAndUpdateSliceIntent(
                getContext(), 3, new ArrayList<>(updatedUris), sliceUri.toString());
        i.putExtra(EXTRA_SLICE_FOLLOWUP, updateSliceIntent);
        PendingIntent disconnectPendingIntent = PendingIntent
                .getActivity(context, 3, i,
                        FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
        forgetPref.setPendingIntent(disconnectPendingIntent);
        psb.addPreference(forgetPref);

        // Update "LE Audio".
        if (enableLeAudioUnicastUi()
        && BluetoothUtils.leAudioUnicastSupported(context)
        && BluetoothUtils.hasAudioProfile(cachedDevice)) {
            boolean isActive = BluetoothUtils.isLeAudioDevice(device);

            Intent intent = new Intent(ACTION_TOGGLE_CHANGED);
            intent.setClass(context, SliceBroadcastReceiver.class);
            intent.putExtra(TOGGLE_TYPE, LE_AUDIO_UNICAST);
            intent.putExtra(TOGGLE_STATE, !isActive);
            intent.putExtra(KEY_EXTRAS_DEVICE, device);

            updatedUris = Arrays.asList(GENERAL_SLICE_URI.toString(), sliceUri.toString());
            updateSliceIntent = updateSliceIntent(getContext(),
                    LE_AUDIO_UNICAST_UPDATE_REQUEST_CODE, new ArrayList<>(updatedUris),
                    sliceUri.toString());
            intent.putExtra(EXTRA_SLICE_FOLLOWUP, updateSliceIntent);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    LE_AUDIO_UNICAST_REQUEST_CODE, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Update set/unset active LE Audio preference
            RowBuilder leAudioPref = new RowBuilder()
                    .setKey("KEY_TOGGLE_LE_UNICAST")
                    .setTitle("LE Audio")
                    .setActionId(0) // TODO: Add a TvSettingsEnums entry for LE Audio
                    .addSwitch(pendingIntent,
                            "LE Audio",
                            isActive);

            psb.addPreference(leAudioPref);
        }

        SpannableStringBuilder bluetoothInfoStringBuilder = new SpannableStringBuilder();

        int battery = btDeviceProvider.getBatteryLevel(device);
        if (battery != BluetoothDevice.BATTERY_LEVEL_UNKNOWN) {
            String batteryText = btDeviceProvider.mapBatteryLevel(context, device, battery);
            final String warning = getString(R.string.settings_bt_battery_low);

            if (btDeviceProvider.isBatteryLow(device) && !btDeviceProvider.hasUpgrade(device)) {
                batteryText = context.getString(
                        R.string.settings_remote_battery_level_with_warning_label, batteryText,
                        warning);
            }

            SpannableStringBuilder styledBatteryText = new SpannableStringBuilder(context.getString(R.string.settings_remote_battery_level, batteryText));
            // Make battery percentage bold
            styledBatteryText.setSpan(new StyleSpan(Typeface.BOLD), styledBatteryText.length() - batteryText.length(), styledBatteryText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            addToBluetoothInfoStringBuilder(bluetoothInfoStringBuilder, styledBatteryText);
        }

        if (mVersionsMap.containsKey(deviceAddr)) {
            Version version = mVersionsMap.get(deviceAddr);
            if (!Version.BAD_VERSION.equals(version)) {
                addToBluetoothInfoStringBuilder(bluetoothInfoStringBuilder, context.getString(R.string.settings_remote_firmware, version.toVersionString()));
            }
            addToBluetoothInfoStringBuilder(bluetoothInfoStringBuilder, context.getString(R.string.settings_remote_serial_number, deviceAddr));
        }

        psb.addScreenTitle(
            new RowBuilder()
                .setTitle(deviceName)
                .setSubtitle(bluetoothInfoStringBuilder)
                .setPageId(0x18200000)); // TvSettingsEnums.CONNECTED_SLICE_DEVICE_ENTRY

        return psb.build();
    }

    private void addToBluetoothInfoStringBuilder(SpannableStringBuilder bluetoothInfoStringBuilder, CharSequence toAdd) {
        if (bluetoothInfoStringBuilder.length() > 0) {
            bluetoothInfoStringBuilder.append("\n");
        }
        bluetoothInfoStringBuilder.append(toAdd);
    }


    // The slice that shows CEC control related information
    private Slice createCecSlice(Uri sliceUri) {
        Context context = getContext();
        PreferenceSliceBuilder psb = new PreferenceSliceBuilder(context, sliceUri);
        psb.addScreenTitle(
                new RowBuilder()
                        .setTitle(getString(R.string.settings_hdmi_cec))
                        .setPageId(0x18300000)); // TvSettingsEnums.CONNECTED_SLICE_HDMICEC
        final boolean isEnabled = PowerUtils.isCecControlEnabled(context);
        Intent intent = new Intent(context, SliceBroadcastReceiver.class)
                .setAction(ACTION_TOGGLE_CHANGED)
                .putExtra(TOGGLE_TYPE, CEC)
                .putExtra(TOGGLE_STATE, !isEnabled);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
        psb.addPreference(new RowBuilder()
                .setTitle(getString(R.string.settings_enable_hdmi_cec))
                .setActionId(0x18310000) // TvSettingsEnums.CONNECTED_SLICE_HDMICEC_ON_OFF
                .addSwitch(pendingIntent, null, isEnabled));
        psb.addPreference(new RowBuilder()
                .setTitle(getString(R.string.settings_cec_explain))
                .setEnabled(false));
        psb.addPreference(new RowBuilder()
                .setTitle(getString(R.string.settings_cec_feature_names))
                .setEnabled(false));

        // Allow the user to choose the behavior of their device when losing active source.
        // This setting should be visible only on playback devices (OTTs/STBs) and it should be
        // available to be toggled only when CEC is enabled.
        if (hdmiControlEnhancedBehavior() && PowerUtils.isPlaybackDevice(context)) {
            final boolean isEnabledGoToSleepOnActiveSourceLost =
                    PowerUtils.isEnabledGoToSleepOnActiveSourceLost(context);
            Intent intentGoToSleepOnActiveSourceLost = new Intent(context,
                    SliceBroadcastReceiver.class)
                    .setAction(ACTION_TOGGLE_CHANGED)
                    .putExtra(TOGGLE_TYPE, POWER_STATE_CHANGE_ON_ACTIVE_SOURCE_LOST)
                    .putExtra(TOGGLE_STATE, !isEnabledGoToSleepOnActiveSourceLost);
            PendingIntent pendingIntentGoToSleepOnActiveSourceLost = PendingIntent.getBroadcast(
                    context, 1, intentGoToSleepOnActiveSourceLost,
                    FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
            psb.addPreference(new RowBuilder()
                    .setTitle(getString(
                            R.string.settings_cec_go_to_sleep_on_active_source_lost_title))
                    .setInfoSummary(getString(
                            R.string.settings_cec_go_to_sleep_on_active_source_lost_description))
                    .addSwitch(pendingIntentGoToSleepOnActiveSourceLost, null,
                            isEnabledGoToSleepOnActiveSourceLost && isEnabled)
                    .setEnabled(isEnabled));
        }
        return psb.build();
    }

    private Slice createFindMyRemoteSlice(Uri sliceUri) {
        Context context = getContext();
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(context, sliceUri);
        psb.addScreenTitle(new RowBuilder()
                .setTitle(getString(R.string.settings_find_my_remote_title))
                .setSubtitle(getString(R.string.find_my_remote_slice_description)));

        if (context.getResources().getBoolean(R.bool.find_my_remote_integration_enabled)) {
            boolean isButtonEnabled = isFindMyRemoteButtonEnabled(context);
            Intent intent = new Intent(context, SliceBroadcastReceiver.class)
                    .setAction(ACTION_TOGGLE_CHANGED)
                    .putExtra(TOGGLE_TYPE, FIND_MY_REMOTE_PHYSICAL_BUTTON_ENABLED_SETTING)
                    .putExtra(TOGGLE_STATE, !isButtonEnabled);
            psb.addPreference(new RowBuilder()
                    .setKey(FIND_MY_REMOTE_PHYSICAL_BUTTON_ENABLED_SETTING)
                    .setTitle(getString(R.string.find_my_remote_integration_title))
                    .setInfoTitleIcon(IconCompat.createWithResource(
                            context, R.drawable.ic_info_24dp))
                    .setInfoSummary(getString(R.string.find_my_remote_integration_hint))
                    .addSwitch(
                            PendingIntent.getBroadcast(
                                    context, 0, intent, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT),
                            isButtonEnabled));
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                new Intent(context, SliceBroadcastReceiver.class)
                        .setAction(ACTION_FIND_MY_REMOTE)
                        .setFlags(FLAG_RECEIVER_FOREGROUND),
                FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);

        psb.addPreference(new RowBuilder()
                .setKey(ACTION_FIND_MY_REMOTE)
                .setTitle(getString(R.string.find_my_remote_play_sound))
                .setPendingIntent(pendingIntent)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_play_arrow))
                .setIconNeedsToBeProcessed(true));
        return psb.build();
    }

    /**
     * Radio Group for backlight mode.
     * 0: Never
     * 1: Standard (Always)
     * 2: Scheduled (Only during nighttime)
     */
    private Slice createBacklightSlice(Uri sliceUri) {
        Context context = getContext();
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(context, sliceUri);
        psb.addScreenTitle(new RowBuilder()
                .setTitle(getString(R.string.settings_backlight_title))
                .setSubtitle(getString(R.string.backlight_slice_description)));

        final String[] backlightModes =
                context.getResources().getStringArray(R.array.backlight_modes);
        final String[] backlightKeys =
                context.getResources().getStringArray(R.array.backlight_keys);
        final String[] backlightHints =
                context.getResources().getStringArray(R.array.backlight_hints);

        for (int i = 0; i < backlightModes.length; i++) {
            final boolean isChecked = getBacklightMode(context) == i;

            final RowBuilder backlightModeRow =
                    new RowBuilder()
                            .setKey(backlightKeys[i])
                            .setTitle(backlightModes[i])
                            .setInfoTitleIcon(IconCompat.createWithResource(
                                    context, R.drawable.ic_info_24dp))
                            .setInfoTitle(backlightModes[i])
                            .setInfoSummary(backlightHints[i])
                            .setRadioGroup(KEY_BACKLIGHT_RADIO_GROUP)
                            .addRadioButton(
                                    getBacklightModeIntent(context, sliceUri, backlightKeys[i]),
                                    isChecked);
            psb.addPreference(backlightModeRow);
        }
        return psb.build();
    }

    private void createAndAddBtDeviceSlicePreferenceFromSet(
            PreferenceSliceBuilder psb,
            Set<String> addresses,
            HashMap<String, BluetoothDevice> addressesToBtDeviceMap) {
        if (psb == null || addresses == null || addresses.isEmpty()
                || addressesToBtDeviceMap == null || addressesToBtDeviceMap.isEmpty()) {
            return;
        }
        final List<String> devicesAddressesList = new ArrayList<>(addresses);
        Collections.sort(devicesAddressesList);
        for (String deviceAddr : devicesAddressesList) {
            psb.addPreference(
                    createBtDeviceSlicePreference(
                            getContext(),
                            getBluetoothDeviceProvider(),
                            addressesToBtDeviceMap.get(deviceAddr)));
        }
    }

    private static PreferenceSliceBuilder.RowBuilder createBtDeviceSlicePreference(
            Context context, BluetoothDeviceProvider provider, BluetoothDevice device) {
        PreferenceSliceBuilder.RowBuilder pref = new PreferenceSliceBuilder.RowBuilder();
        pref.setKey(device.getAddress());
        updateBtDevicePreference(context, provider, device, pref);

        RestrictedLockUtils.EnforcedAdmin admin =
                RestrictedLockUtilsInternal.checkIfRestrictionEnforced(context,
                        UserManager.DISALLOW_CONFIG_BLUETOOTH, UserHandle.myUserId());
        if (admin == null) {
            Uri targetSliceUri = SlicesUtil.getDeviceUri(device.getAddress());
            pref.setTargetSliceUri(targetSliceUri.toString());
        } else {
            Intent intent = RestrictedLockUtils.getShowAdminSupportDetailsIntent(context, admin);
            intent.putExtra(DevicePolicyManager.EXTRA_RESTRICTION,
                    UserManager.DISALLOW_CONFIG_BLUETOOTH);
            pref.setPendingIntent(PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE));
        }
        return pref;
    }

    private static void updateBtDevicePreference(Context context, BluetoothDeviceProvider provider,
            BluetoothDevice device, PreferenceSliceBuilder.RowBuilder pref) {
        int batteryLevel = provider.getBatteryLevel(device);
        pref.setKey(device.getAddress());
        pref.setTitle(BluetoothUtils.getName(device));
        if (provider.hasUpgrade(device)) {
            pref.setSubtitle(context.getString(R.string.settings_bt_update_available));
        } else {
            if (batteryLevel != BluetoothDevice.BATTERY_LEVEL_UNKNOWN) {
                if (provider.isBatteryLow(device)) {
                    pref.setSubtitle(context.getString(R.string.settings_bt_battery_low_warning));
                } else {
                    String batteryText = provider.mapBatteryLevel(context, device, batteryLevel);
                    pref.setSubtitle(context.getString(R.string.settings_remote_battery_level,
                            batteryText));
                }
            } else {
                boolean isConnected = BluetoothUtils.isConnected(device)
                        && BluetoothUtils.getCachedBluetoothDevice(context, device) != null
                        && BluetoothUtils.getCachedBluetoothDevice(context, device).isConnected();
                pref.setSubtitle(isConnected
                        ? context.getString(R.string.bluetooth_connected_status)
                        : context.getString(R.string.bluetooth_disconnected_status));
            }
        }
        pref.setIcon(IconCompat.createWithResource(
                context, BluetoothUtils.getIcon(context, device)));
        pref.setIconNeedsToBeProcessed(true);
    }

    private final BluetoothDeviceProvider mLocalBluetoothDeviceProvider =
            new LocalBluetoothDeviceProvider() {
                @Override
                BluetoothDeviceProvider getHostBluetoothDeviceProvider() {
                    return getBluetoothDeviceProvider();
                }
            };

    private boolean showCecInConnectedSettings() {
        return Configuration.get(getContext()).isEnabled(R.bool.show_cec_in_connected_settings);
    }

    private boolean showDisconnectButton(BluetoothDevice device, Context context) {
        if (DISCONNECT_PREFERENCE_ENABLED) {
            return true;
        }
        return !BluetoothUtils.isRemoteClass(device) && !BluetoothUtils.isRemote(context, device)
                && !BluetoothUtils.isBluetoothDeviceMetadataInList(getContext(), device,
                BluetoothDevice.METADATA_MODEL_NAME,
                R.array.disconnect_button_hidden_device_model_names);
    }
}
