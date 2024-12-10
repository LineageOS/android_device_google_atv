/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.tv.btservices.syncwork;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;
import static android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND;

import static com.google.android.tv.btservices.settings.SlicesUtil.getBacklightMode;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker.Result;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RemoteSyncWorker extends Worker {
    private static final String TAG = "RemoteSyncWorker";
    private final Context mContext;

    static final String ACTION_BACKLIGHT = "com.google.android.tv.BACKLIGHT";
    static final String BACKLIGHT_MODE = "BACKLIGHT_MODE";
    static final String BACKLIGHT_MODE_SETTING = "backlight_mode_setting";

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param context      the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public RemoteSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    /**
     * Overrides the default doWork method to handle checking and provisioning the device.
     */
    @Override
    public Result doWork() {
        try {
            Log.i(TAG, "Broadcast an intent to the remote to calibrate its current time.");
            return doSynchronizedWork();
        } catch (Exception e) {
            Log.e(TAG, "Could not run periodic remote sync request", e);
            // Default policy is EXPONENTIAL with a delay of 30 seconds.
            return Result.retry();
        }
    }

    private Result doSynchronizedWork() {
        int modes = getBacklightMode(mContext);

        // Only need to calibrate when user choose option Schedules.
        mContext.sendBroadcast(
                new Intent(ACTION_BACKLIGHT)
                        .putExtra(BACKLIGHT_MODE, modes)
                        .setFlags(FLAG_INCLUDE_STOPPED_PACKAGES | FLAG_RECEIVER_FOREGROUND
                                | FLAG_RECEIVER_INCLUDE_BACKGROUND),
                "com.google.android.tv.permission.BACKLIGHT");

        return Result.success();
    }
}
