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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;


public class RemoteSyncWorkManager {
    private static final String TAG = "RemoteSyncWorkManager";
    //private static final WorkManager mWorkManager;

    public static final String WORK_NAME = "SYNC_REMOTE_PERIODIC";

    public RemoteSyncWorkManager(){}
    private static final WorkManager workManager = WorkManager.getInstance();

    public static void schedulePeriodicSyncs() {
        Log.i(TAG, "Scheduling periodic remote time syncs");
        // Run periodically
        final PeriodicWorkRequest periodicSyncRequest = getDailySyncRequest();

        try {
            // Cancel the previous work. Maybe user reconnect remote in one day.
            workManager.cancelUniqueWork(WORK_NAME);
            // We only need one unique daily work in queue. Submit the WorkRequest to the system.
            final Operation enqueueOperation = workManager
                    .enqueueUniquePeriodicWork(
                            WORK_NAME,
                            // If there is existing pending (uncompleted) work with the same
                            // PERIODIC_SYNC_WORK_NAME name, do nothing.
                            ExistingPeriodicWorkPolicy.KEEP,
                            periodicSyncRequest
                    );
            // Check that the request has been successfully enqueued.
            enqueueOperation.getResult().get();

        } catch (Exception e) {
            Log.e(TAG, "Could not enqueue periodic remote sync request", e);
        }
    }

    private static PeriodicWorkRequest getDailySyncRequest() {
        final int SYNC_PERIODIC_WORK_INTERVAL = 1;
        return new PeriodicWorkRequest.Builder(RemoteSyncWorker.class
                , SYNC_PERIODIC_WORK_INTERVAL
                , TimeUnit.DAYS)
                .addTag(WORK_NAME)
                .build();
    }
}


