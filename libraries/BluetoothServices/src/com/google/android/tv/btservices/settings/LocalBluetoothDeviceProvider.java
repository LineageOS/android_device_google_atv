// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.google.android.tv.btservices.remote.DfuManager;
import com.google.android.tv.btservices.remote.RemoteProxy;
import com.google.android.tv.btservices.remote.Version;
import com.google.android.tv.btservices.R;

/**
 * Local provider proxy to customize events.
 */
abstract class LocalBluetoothDeviceProvider implements BluetoothDeviceProvider {

    abstract BluetoothDeviceProvider getHostBluetoothDeviceProvider();

    @Override
    public int getBatteryLevel(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            return provider.getBatteryLevel(device);
        }
        return BluetoothDevice.BATTERY_LEVEL_UNKNOWN;
    }

    @Override
    public String mapBatteryLevel(Context context, BluetoothDevice device, int level) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            return provider.mapBatteryLevel(context, device, level);
        }
        return context.getString(R.string.settings_remote_battery_level_percentage_label, level);
    }

    @Override
    public Version getVersion(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            return provider.getVersion(device);
        }
        return Version.BAD_VERSION;
    }

    @Override
    public boolean hasUpgrade(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            return provider.hasUpgrade(device);
        }
        return false;
    }

    @Override
    public boolean isBatteryLow(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            return provider.isBatteryLow(device);
        }
        return false;
    }

    @Override
    public RemoteProxy.DfuResult getDfuState(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            return provider.getDfuState(device);
        }
        return null;
    }

    @Override
    public void startDfu(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.startDfu(device);
        }
    }

    @Override
    public void connectDevice(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.connectDevice(device);
        }
    }

    @Override
    public void disconnectDevice(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.disconnectDevice(device);
        }
    }

    @Override
    public void forgetDevice(BluetoothDevice device) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.forgetDevice(device);
        }
    }

    @Override
    public void renameDevice(BluetoothDevice device, String newName) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.renameDevice(device, newName);
        }
    }

    @Override
    public void addListener(Listener listener) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.addListener(listener);
        }
    }

    @Override
    public void removeListener(Listener listener) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.removeListener(listener);
        }
    }

    @Override
    public void addListener(DfuManager.Listener listener) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.addListener(listener);
        }
    }

    @Override
    public void removeListener(DfuManager.Listener listener) {
        BluetoothDeviceProvider provider = getHostBluetoothDeviceProvider();
        if (provider != null) {
            provider.removeListener(listener);
        }
    }
}
