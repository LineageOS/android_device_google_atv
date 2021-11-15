// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.remote;

import android.util.Log;
import com.google.android.tv.btservices.remote.Version.OverrideVersion;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Transform from a file to a series of packet for Device Field Update (DFU).
public abstract class DfuBinary implements Comparable<DfuBinary> {

    private static final String TAG = "Atv.DfuBinary";

    protected List<Packet> mPackets = new ArrayList<>();
    protected Version mVersion;

    public interface Factory {
        DfuBinary build(InputStream fin, boolean override);
    }

    protected DfuBinary() {}

    protected void initialize(InputStream fin, boolean override) {
        try {
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            fin.close();
            if (bytes.length < 64) {
                Log.e(TAG, "bad dfu binary");
                return;
            }
            mVersion = readVersion(bytes);
            if (override) {
                mVersion = new OverrideVersion(mVersion);
            }

            buildHeader(bytes);
            buildPackets(bytes);
            buildTail(bytes);
        } catch (Exception e) {
            Log.e(TAG, "error in opening ota file: " + e);
        }
    }

    protected abstract Version readVersion(byte[] buf);

    protected abstract void buildHeader(byte[] buf);

    protected abstract void buildPackets(byte[] buf);

    protected abstract void buildTail(byte[] buf);

    public Version getVersion() {
        return mVersion;
    }

    public Packet[] getPackets() {
        return mPackets.toArray(new Packet[0]);
    }

    @Override
    public int compareTo(DfuBinary other) {
        return getVersion().compareTo(other.getVersion());
    }
}
