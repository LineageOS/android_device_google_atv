package com.android.tv.mdnsoffloadmanager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WorkerThread
public class InterfaceOffloadManager {

    private static final String TAG = InterfaceOffloadManager.class.getSimpleName();

    private final String mNetworkInterface;
    private final OffloadIntentStore mOffloadIntentStore;
    private final OffloadWriter mOffloadWriter;
    private final Set<Integer> mCurrentOffloadedRecordKeys = new HashSet<>();
    private final Set<String> mCurrentPassthroughQName = new HashSet<>();
    private boolean mIsNetworkAvailable = false;

    InterfaceOffloadManager(
            @NonNull String networkInterface,
            @NonNull OffloadIntentStore offloadIntentStore,
            @NonNull OffloadWriter offloadWriter) {
        mNetworkInterface = networkInterface;
        mOffloadIntentStore = offloadIntentStore;
        mOffloadWriter = offloadWriter;
    }

    void onVendorServiceConnected() {
        refreshProtocolResponses();
        refreshPassthroughList();
    }

    void onAppIdAllowlistUpdated() {
        refreshProtocolResponses();
        refreshPassthroughList();
    }

    void onNetworkAvailable() {
        String msg = "Network interface {" + mNetworkInterface + "} is connected." +
                " Offloading all stored data.";
        Log.d(TAG, msg);
        mIsNetworkAvailable = true;
        refreshProtocolResponses();
        refreshPassthroughList();
    }

    void onNetworkLost() {
        String msg = "Network interface {" + mNetworkInterface + "} was disconnected."
                + " Clearing all associated data.";
        Log.d(TAG, msg);
        mIsNetworkAvailable = false;
        clearProtocolResponses();
        clearPassthroughList();
    }

    void onVendorServiceDisconnected() {
        mCurrentOffloadedRecordKeys.clear();
        mCurrentPassthroughQName.clear();
    }

    void refreshProtocolResponses() {
        if (!mIsNetworkAvailable) {
            return;
        }
        applyOffloadIntents(mOffloadIntentStore.getOffloadIntentsForInterface(mNetworkInterface));
    }

    void refreshPassthroughList() {
        if (!mIsNetworkAvailable) {
            return;
        }
        applyPassthroughIntents(
                mOffloadIntentStore.getPassthroughIntentsForInterface(mNetworkInterface));
    }

    private void clearProtocolResponses() {
        applyOffloadIntents(Collections.emptySet());
    }

    private void clearPassthroughList() {
        applyPassthroughIntents(Collections.emptyList());
    }

    private void applyOffloadIntents(Collection<OffloadIntentStore.OffloadIntent> offloadIntents) {
        if (!mOffloadWriter.isVendorServiceConnected()) {
            Log.e(TAG, "Vendor service disconnected, cannot apply mDNS offload state");
            return;
        }
        Collection<Integer> deleted = mOffloadWriter.deleteOffloadData(mCurrentOffloadedRecordKeys);
        mCurrentOffloadedRecordKeys.removeAll(deleted);
        Collection<Integer> offloaded = mOffloadWriter.writeOffloadData(
                mNetworkInterface, offloadIntents);
        mCurrentOffloadedRecordKeys.addAll(offloaded);
    }

    private void applyPassthroughIntents(
            List<OffloadIntentStore.PassthroughIntent> passthroughIntents) {
        if (!mOffloadWriter.isVendorServiceConnected()){
            Log.e(TAG, "Vendor service disconnected, cannot apply mDNS passthrough state");
            return;
        }
        Collection<String> deleted = mOffloadWriter.deletePassthroughData(
                mNetworkInterface, mCurrentPassthroughQName);
        mCurrentPassthroughQName.removeAll(deleted);
        Collection<String> added = mOffloadWriter.writePassthroughData(
                mNetworkInterface, passthroughIntents);
        mCurrentPassthroughQName.addAll(added);
    }

}
