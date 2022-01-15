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

import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.CONTINUE;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_CONNECT;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_DISCONNECT;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_FORGET;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_RENAME;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_UPDATE;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.YES;
import static com.google.android.tv.btservices.settings.ConnectedDevicesSliceProvider.KEY_EXTRAS_DEVICE;
import static com.google.android.tv.btservices.settings.SlicesUtil.DIRECTION_BACK;
import static com.google.android.tv.btservices.settings.SlicesUtil.EXTRAS_DIRECTION;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.tv.btservices.BluetoothDeviceService;
import com.google.android.tv.btservices.BluetoothUtils;
import com.google.android.tv.btservices.SimplifiedConnection;

public class ResponseActivity extends Activity implements
        com.google.android.tv.btservices.settings.BluetoothDeviceProvider.Listener,
        ResponseFragment.Listener {

    private boolean mBtDeviceServiceBound;
    private BluetoothDevice mDevice;
    private BluetoothDeviceService.LocalBinder mBtDeviceServiceBinder;

    private final ServiceConnection mBtDeviceServiceConnection = new SimplifiedConnection() {

        @Override
        protected void cleanUp() {
            if (mBtDeviceServiceBinder != null) {
                mBtDeviceServiceBinder.removeListener(ResponseActivity.this);
            }
            mBtDeviceServiceBound = false;
            mBtDeviceServiceBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBtDeviceServiceBinder = (BluetoothDeviceService.LocalBinder) service;
            mBtDeviceServiceBound = true;
            mBtDeviceServiceBinder.addListener(ResponseActivity.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice = getIntent().getParcelableExtra(KEY_EXTRAS_DEVICE);
        bindService(new Intent(this, BluetoothUtils.getBluetoothDeviceServiceClass(this)),
                mBtDeviceServiceConnection, Context.BIND_AUTO_CREATE);
        ResponseFragment responseFragment = new ResponseFragment();
        responseFragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(android.R.id.content, responseFragment)
                .commit();
    }

    @Override
    public void onDestroy() {
        if (mBtDeviceServiceBound) {
            mBtDeviceServiceBinder.removeListener(this);
            unbindService(mBtDeviceServiceConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onChoice(String key, int choice) {
        BluetoothDeviceProvider provider = getBluetoothDeviceProvider();
        Intent i = new Intent();

        if (provider == null) {
            return;
        }
        if (key == null) {
            setResult(RESULT_OK, i);
            finish();
        }
        switch (key) {
            case KEY_CONNECT:
                if (choice == YES) {
                    provider.connectDevice(mDevice);
                    i.putExtra(EXTRAS_DIRECTION, DIRECTION_BACK);
                }
                break;
            case KEY_DISCONNECT:
                if (choice == YES) {
                    provider.disconnectDevice(mDevice);
                    i.putExtra(EXTRAS_DIRECTION, DIRECTION_BACK);
                }
                break;
            case KEY_FORGET:
                if (choice == YES) {
                    provider.forgetDevice(mDevice);
                    i.putExtra(EXTRAS_DIRECTION, DIRECTION_BACK);
                }
                break;
            case KEY_UPDATE:
                if (choice == CONTINUE && mDevice != null) {
                    Intent intent = new Intent(this, RemoteDfuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(RemoteDfuActivity.EXTRA_BT_ADDRESS, mDevice.getAddress());
                    startActivity(intent);
                }
                break;
        }
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onText(String key, String text) {
        BluetoothDeviceProvider provider = getBluetoothDeviceProvider();
        if (KEY_RENAME.equals(key)) {
            if (mDevice != null) {
                provider.renameDevice(mDevice, text);
            }
        }
        Intent i = new Intent();
        i.putExtra(EXTRAS_DIRECTION, DIRECTION_BACK);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onDeviceUpdated(BluetoothDevice device) {
    }

    private BluetoothDeviceProvider getBluetoothDeviceProvider() {
        return mBtDeviceServiceBinder;
    }
}