/*
 * Copyright (C) 2025 The Android Open Source Project
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

import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_CONNECT;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_DISCONNECT;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_FORGET;
import static com.google.android.tv.btservices.settings.BluetoothDevicePreferenceFragment.KEY_UPDATE;
import static com.google.android.tv.btservices.settings.ConnectedDevicesSliceProvider.KEY_EXTRAS_DEVICE;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import com.android.tv.twopanelsettings.FullScreenDialogFragment;
import com.android.tv.twopanelsettings.FullScreenDialogFragmentActivity;
import com.google.android.tv.btservices.BluetoothDeviceService;
import com.google.android.tv.btservices.BluetoothUtils;
import com.google.android.tv.btservices.R;
import com.google.android.tv.btservices.SimplifiedConnection;

/**
 * Dialog Activity for Bluetooth Action dialogs. The Positive Action does something different
 * depending on the ARG_KEY that is passed in.
 */
public class DialogResponseActivity extends FullScreenDialogFragmentActivity
    implements com.google.android.tv.btservices.settings.BluetoothDeviceProvider.Listener {

  static final String DIALOG_ARG_KEY = "arg_key";
  static final String DIALOG_ARG_TITLE = "arg_title";
  static final String DIALOG_ARG_SUMMARY = "arg_summary";
  static final String DIALOG_ARG_ICON = "arg_icon";
  static final String DIALOG_ARG_NAME = "arg_name";

  private boolean mBtDeviceServiceBound;
  private BluetoothDevice mDevice;
  private BluetoothDeviceService.LocalBinder mBtDeviceServiceBinder;

  private final ServiceConnection mBtDeviceServiceConnection =
      new SimplifiedConnection() {

        @Override
        protected void cleanUp() {
          if (mBtDeviceServiceBinder != null) {
            mBtDeviceServiceBinder.removeListener(DialogResponseActivity.this);
          }
          mBtDeviceServiceBound = false;
          mBtDeviceServiceBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
          mBtDeviceServiceBinder = (BluetoothDeviceService.LocalBinder) service;
          mBtDeviceServiceBound = true;
          mBtDeviceServiceBinder.addListener(DialogResponseActivity.this);
        }
      };

  public Bundle provideArguments() {
    Intent intent = getIntent();
    FullScreenDialogFragment.DialogBuilder builder =
      new FullScreenDialogFragment.DialogBuilder()
        .setTitle(getTitle(intent))
        .setPositiveButton(getString(R.string.settings_choices_yes))
        .setNegativeButton(getString(R.string.settings_choices_no));
    if (intent.hasExtra(DIALOG_ARG_ICON)) {
      builder.setIcon(Icon.createWithResource(this, intent.getIntExtra(DIALOG_ARG_ICON, 0)));
    }
    return builder.build();
  }

  public FullScreenDialogFragmentActivity.OnPositiveActionClickedListener
      onPositiveActionClicked() {
    Intent intent = getIntent();
    return () -> {
      onAction(intent.getStringExtra(DIALOG_ARG_KEY));
    };
  }

  public FullScreenDialogFragmentActivity.OnNegativeActionClickedListener
      onNegativeActionClicked() {
    return () -> {
      finish();
    };
  }

  @Override
  public Drawable getDrawableIconForDialog() {
    return null;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDevice = getIntent().getParcelableExtra(KEY_EXTRAS_DEVICE);
    bindService(
        new Intent(this, BluetoothUtils.getBluetoothDeviceServiceClass(this)),
        mBtDeviceServiceConnection,
        Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onDestroy() {
    if (mBtDeviceServiceBound) {
      mBtDeviceServiceBinder.removeListener(this);
      unbindService(mBtDeviceServiceConnection);
    }
    super.onDestroy();
  }

  public void onAction(String dialogKey) {
    BluetoothDeviceProvider provider = getBluetoothDeviceProvider();
    Intent i = new Intent().putExtras(getIntent());
    if (provider == null) {
      return;
    }
    if (dialogKey == null) {
      setResult(RESULT_OK, i);
      finish();
      return;
    }
    switch (dialogKey) { // NOPMD: SwitchStmtsShouldHaveDefault
      case KEY_FORGET -> {
        provider.forgetDevice(mDevice);
        setResult(RESULT_OK, i);
      }
      case KEY_CONNECT -> {
        provider.connectDevice(mDevice);
        setResult(RESULT_OK, i);
      }
      case KEY_DISCONNECT -> {
        provider.disconnectDevice(mDevice);
        setResult(RESULT_OK, i);
      }
      case KEY_UPDATE -> {
        Intent intent = new Intent(this, RemoteDfuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(RemoteDfuActivity.EXTRA_BT_ADDRESS, mDevice.getAddress());
        startActivity(intent);
        setResult(RESULT_OK, i);
      }
    }
    finish();
  }

  @Override
  public void onDeviceUpdated(BluetoothDevice device) {
    getContentResolver().notifyChange(SlicesUtil.GENERAL_SLICE_URI, null);
    getContentResolver().notifyChange(SlicesUtil.getDeviceUri(device.getAddress()), null);
  }

  private String getTitle(Intent intent) {
    Bundle args = intent.getExtras();
    String name = args.getString(DIALOG_ARG_NAME);
    if (!TextUtils.isEmpty(name)) {
      return getString(args.getInt(DIALOG_ARG_TITLE), name);
    }
    return getString(args.getInt(DIALOG_ARG_TITLE));
  }

  private BluetoothDeviceProvider getBluetoothDeviceProvider() {
    return mBtDeviceServiceBinder;
  }
}
