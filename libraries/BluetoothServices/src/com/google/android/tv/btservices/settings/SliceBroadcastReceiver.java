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

import static com.google.android.tv.btservices.settings.ConnectedDevicesSliceProvider.ACTION_TOGGLE_CHANGED;
import static com.google.android.tv.btservices.settings.SlicesUtil.CEC_SLICE_URI;
import static com.google.android.tv.btservices.settings.SlicesUtil.DIRECTION_BACK;
import static com.google.android.tv.btservices.settings.SlicesUtil.EXTRAS_DIRECTION;
import static com.google.android.tv.btservices.settings.SlicesUtil.EXTRAS_SLICE_URI;
import static com.google.android.tv.btservices.settings.SlicesUtil.GENERAL_SLICE_URI;
import static com.google.android.tv.btservices.settings.SlicesUtil.notifyToGoBack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.tv.btservices.PowerUtils;

/**
 * This broadcast receiver handles two cases:
 * (a) CEC control toggle.
 * (b) Handle the followup pending intent for "rename"/"forget" preference to notify TvSettings UI
 * flow to go back.
 */
public class SliceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SliceBroadcastReceiver";
    static final String CEC = "CEC";
    static final String TOGGLE_TYPE = "TOGGLE_TYPE";
    static final String TOGGLE_STATE = "TOGGLE_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle CEC control toggle.
        final String action = intent.getAction();
        final boolean isChecked = intent.getBooleanExtra(TOGGLE_STATE, false);
        if (ACTION_TOGGLE_CHANGED.equals(action)
                && CEC.equals(intent.getStringExtra(TOGGLE_TYPE))) {
            PowerUtils.enableCecControl(context, isChecked);
            context.getContentResolver().notifyChange(CEC_SLICE_URI, null);
            context.getContentResolver().notifyChange(GENERAL_SLICE_URI, null);
            return;
        }

        // Notify TvSettings to go back to the previous level.
        String direction = intent.getStringExtra(EXTRAS_DIRECTION);
        if (DIRECTION_BACK.equals(direction)) {
            notifyToGoBack(context, Uri.parse(intent.getStringExtra(EXTRAS_SLICE_URI)));
        }
    }
}
