package com.google.android.tv.btservices.pairing.profiles;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import java.util.List;

public interface PairingProfileWrapper {

    BluetoothProfile getProxy();

    List<BluetoothDevice> getConnectedDevices();

    int getConnectionState(BluetoothDevice device);

    boolean connect(BluetoothDevice device);

    boolean disconnect(BluetoothDevice device);

    boolean setPriority(BluetoothDevice device, int priority);

}
