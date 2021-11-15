// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.remote;

import com.google.android.tv.btservices.remote.TransportUtils;

public abstract class Packet {

    private byte[] mValue;
    private long mTimestamp;
    private boolean mWaitForResponse;
    private byte mRequestType;

    protected Packet(byte[] value, byte reqType, boolean waitForResponse) {
        mValue = value;
        mTimestamp = System.currentTimeMillis();
        mRequestType = reqType;
        mWaitForResponse = waitForResponse;
    }

    public byte getRequestType() {
        return mRequestType;
    }

    public byte[] getValue() {
        return mValue;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean waitForResponse() {
        return mWaitForResponse;
    }

    public abstract boolean transportPacket(Transport transport);

    @Override
    public String toString() {
        return "Req[" + getRequestType() + "]: " + TransportUtils.bytesToString(mValue);
    }

    public static class Read extends Packet {
        protected Read(byte[] value, byte reqType, boolean waitForResponse) {
            super(value, reqType, true /* waitForResponse */);
        }

        @Override
        public boolean transportPacket(Transport transport) {
            return transport.read(getRequestType());
        }
    }

    public static class Write extends Packet {
        protected Write(byte[] value, byte reqType, boolean waitForResponse) {
            super(value, reqType, waitForResponse);
        }

        @Override
        public boolean transportPacket(Transport transport) {
            return transport.write(getRequestType(), getValue());
        }
    }

    // The meta packet does not transfer bits over gatt. Instead, it's a request related to the gatt
    // or bluetooth connection (MTU change, clear gatt DB).
    public static class Meta extends Packet {
        protected Meta(byte reqType) {
            super(null, reqType, false /* waitForResponse */);
        }

        @Override
        public boolean transportPacket(Transport transport) {
            return transport.meta(getRequestType());
        }
    }
}
