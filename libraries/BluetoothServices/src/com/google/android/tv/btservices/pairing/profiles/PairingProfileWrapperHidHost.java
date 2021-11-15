// Copyright 2019 Google LLC. All Rights Reserved.

package com.google.android.tv.btservices.pairing.profiles;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidHost;
import android.bluetooth.BluetoothProfile;

import java.util.List;

public class PairingProfileWrapperHidHost implements PairingProfileWrapper {

    private BluetoothHidHost mProxy;

    public PairingProfileWrapperHidHost(BluetoothProfile proxy) {
        mProxy = (BluetoothHidHost) proxy;
    }

    @Override
    public BluetoothProfile getProxy() {
        return mProxy;
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        return mProxy.getConnectedDevices();
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        return mProxy.getConnectionState(device);
    }

    @Override
    public boolean connect(BluetoothDevice device) {
        return mProxy.connect(device);
    }

    @Override
    public boolean disconnect(BluetoothDevice device) {
        return mProxy.disconnect(device);
    }

    @Override
    public boolean setPriority(BluetoothDevice device, int priority) {
        return mProxy.setPriority(device, priority);
    }
}
