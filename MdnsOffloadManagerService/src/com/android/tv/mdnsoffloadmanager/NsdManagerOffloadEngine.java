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

package com.android.tv.mdnsoffloadmanager;

import android.net.nsd.OffloadEngine;
import android.os.Binder;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HexFormat;

import device.google.atv.mdns_offload.IMdnsOffloadManager.OffloadServiceInfo;

/**
 * Offload engine for the NsdManager offload interface.
 * NsdManager needs at least one different offload engine per interface.
 * To keep the differentiation on the network interface we use separate NsdManagerOffloadEngine
 * instances that connect to a single MdnsOffloadManagerService.
 */
class NsdManagerOffloadEngine implements OffloadEngine {
    private static final String TAG = NsdManagerOffloadEngine.class.getSimpleName();

    private final Map<android.net.nsd.OffloadServiceInfo.Key, Integer> mRecordKeys =
            new HashMap<>();
    private final MdnsOffloadManagerService mOffloadManager;
    private final String mNetworkInterface;
    private final Binder mClientToken;

    public NsdManagerOffloadEngine(MdnsOffloadManagerService offloadManager, String iface) {
        mOffloadManager = offloadManager;
        mNetworkInterface = iface;
        mClientToken = new Binder("OffloadEngine[%s]".formatted(iface));
    }

    @WorkerThread
    @Override
    public void onOffloadServiceUpdated(android.net.nsd.OffloadServiceInfo info) {
        OffloadServiceInfo internalInfo = new OffloadServiceInfo();
        internalInfo.serviceName = info.getKey().getServiceName();
        internalInfo.serviceType = info.getKey().getServiceType();
        internalInfo.deviceHostName = info.getHostname();
        internalInfo.rawOffloadPacket = info.getOffloadPayload();

        // DO NOT SUBMIT - workaround http://b/323169340
        Log.e(TAG, "eGVB: onOffloadServiceUpdated %s".formatted(Arrays.toString(info.getOffloadPayload())));

        int recordKey = mOffloadManager
                .addProtocolResponses(mNetworkInterface, internalInfo, mClientToken);

        mRecordKeys.put(info.getKey(), recordKey);
    }

    @WorkerThread
    @Override
    public void onOffloadServiceRemoved(android.net.nsd.OffloadServiceInfo info) {
        Integer recordKey = mRecordKeys.remove(info.getKey());
        if (recordKey == null) {
            Log.e(TAG, "Unknown OffloadService key %s".formatted(info.getKey()));
            return;
        }
        mOffloadManager.removeProtocolResponses(recordKey, mClientToken);
    }

    /**
     * Clean up any dangling protocol responses created by NsdManager for this interface.
     */
    @WorkerThread
    void clearOffloadServices() {
        for (Integer recordKey : mRecordKeys.values()) {
            mOffloadManager.removeProtocolResponses(recordKey, mClientToken);
        }
        mRecordKeys.clear();
    }
}
