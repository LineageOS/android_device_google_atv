// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.google.android.tv.btservices.remote.DfuManager;
import com.google.android.tv.btservices.remote.RemoteProxy.DfuResult;
import com.google.android.tv.btservices.remote.Version;

public interface BluetoothDeviceProvider {

    interface Listener {
        void onDeviceUpdated(BluetoothDevice device);
    }

    int getBatteryLevel(BluetoothDevice device);

    String mapBatteryLevel(Context context, BluetoothDevice device, int level);

    Version getVersion(BluetoothDevice device);

    boolean hasUpgrade(BluetoothDevice device);

    boolean isBatteryLow(BluetoothDevice device);

    DfuResult getDfuState(BluetoothDevice device);

    void startDfu(BluetoothDevice device);

    void connectDevice(BluetoothDevice device);

    void disconnectDevice(BluetoothDevice device);

    void forgetDevice(BluetoothDevice device);

    void renameDevice(BluetoothDevice device, String newName);

    void addListener(Listener listener);

    void removeListener(Listener listener);

    void addListener(DfuManager.Listener listener);

    void removeListener(DfuManager.Listener listener);
}
