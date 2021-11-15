// Copyright 2019 Google LLC. All Rights Reserved.

package com.google.android.tv.btservices.pairing;

import static com.google.android.tv.btservices.pairing.BluetoothPairer.STATUS_CANCELLED;
import static com.google.android.tv.btservices.pairing.BluetoothPairer.STATUS_DONE;
import static com.google.android.tv.btservices.pairing.BluetoothPairer.STATUS_ERROR;
import static com.google.android.tv.btservices.pairing.BluetoothPairer.STATUS_TIMEOUT;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

import com.google.android.tv.btservices.BluetoothUtils;

import java.util.ArrayList;
import java.util.List;

public class BluetoothPairingService extends Service implements BluetoothScanner.Listener,
        BluetoothPairer.Listener {

    private static final String TAG = "Atv.BtPairingService";

    private final Binder mBinder = new LocalBinder();
    private List<ScanningListener> mScanningListenerList = new ArrayList<>();
    private List<PairingListener> mPairingListenerList = new ArrayList<>();
    private BluetoothPairer mBluetoothPairer;

    private final Handler mHandler = new Handler();

    public static final int STATUS_FOUND = 1;
    public static final int STATUS_UPDATED = 2;
    public static final int STATUS_LOST = 3;

    private static final long POST_SCANNING_PRE_PAIRING_PAUSE_MS = 500;

    public interface ScanningListener {
        void updateScanning(boolean isScanning);
        void updateDevice(BluetoothDevice device, int status);
    }

    public interface PairingListener {
        void updatePairingStatus(BluetoothDevice device, int status);
    }

    public class LocalBinder extends Binder {

        public void addScanningListener(ScanningListener listener) {
            if (!mScanningListenerList.contains(listener)) {
                mScanningListenerList.add(listener);
                resetScanning();
            }
        }

        public void addPairingListener(PairingListener listener) {
            if (!mPairingListenerList.contains(listener)) {
                mPairingListenerList.add(listener);
            }
        }

        public void removeScanningListener(ScanningListener listener) {
            mScanningListenerList.remove(listener);
            if (mScanningListenerList.isEmpty()) {
                stopScanning();
            }
        }

        public void removePairingListener(PairingListener listener) {
            mPairingListenerList.remove(listener);
        }

        public void restartScanning() {
            resetScanning();
        }

        public void pairDevice(BluetoothDevice device) {
            pair(device, true);
        }

        public void connectPairedDevice(BluetoothDevice device) {
            // Need to preserve bond for already paired but not connected device
            pair(device, false);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void resetScanning() {
        stopScanning();
        if (pairingInProgress()) {
            return;
        }
        if (!mScanningListenerList.isEmpty()) {
            BluetoothScanner.startListening(this, this);
            mScanningListenerList.forEach(listener -> listener.updateScanning(true));
        }
    }

    private void stopScanning() {
        BluetoothScanner.stopListening(this);
        mScanningListenerList.forEach(listener -> listener.updateScanning(false));
    }

    private boolean pairingInProgress() {
        return mBluetoothPairer != null;
    }

    private void pair(BluetoothDevice device, boolean unpairOnFail) {
        if (pairingInProgress()) {
            return;
        }
        Integer pairingProfile = getPairingProfile(device);
        if (pairingProfile != null) {
            stopScanning();
            mHandler.postDelayed(() -> {
                try {
                    mBluetoothPairer = new BluetoothPairer(this, pairingProfile);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid device type", e);
                    return;
                }
                mBluetoothPairer.startPairing(device, this, unpairOnFail);
            }, POST_SCANNING_PRE_PAIRING_PAUSE_MS);
        }
    }

    private Integer getPairingProfile(BluetoothDevice device) {
        if (BluetoothUtils.isBluetoothHeadset(device)) {
            return BluetoothProfile.A2DP;
        } else if (BluetoothUtils.isRemoteClass(device)) {
            return BluetoothProfile.HID_HOST;
        } else {
            return null;
        }
    }

    /** BluetoothScanner.Listener implementation */
    @Override
    public void onDeviceAdded(BluetoothDevice device) {
        mScanningListenerList.forEach(listener -> listener.updateDevice(device, STATUS_FOUND));
    }

    /** BluetoothScanner.Listener implementation */
    @Override
    public void onDeviceChanged(BluetoothDevice device) {
        mScanningListenerList.forEach(listener -> listener.updateDevice(device, STATUS_UPDATED));
    }

    /** BluetoothScanner.Listener implementation */
    @Override
    public void onDeviceRemoved(BluetoothDevice device) {
        mScanningListenerList.forEach(listener -> listener.updateDevice(device, STATUS_LOST));
    }

    /** BluetoothPairer.Listener implementation */
    @Override
    public void onStatusChanged(BluetoothDevice device, int status) {
        if (device != null) {
            mPairingListenerList.forEach(listener -> listener.updatePairingStatus(device, status));
        }
        if (mBluetoothPairer != null && (status == STATUS_ERROR || status == STATUS_CANCELLED
                || status == STATUS_TIMEOUT || status == STATUS_DONE)) {
            mBluetoothPairer.dispose();
            mBluetoothPairer = null;
            resetScanning();
        }
    }
}
