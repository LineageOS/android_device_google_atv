// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.remote;

public class Version implements Comparable<Version> {

    public static class OverrideVersion extends Version {
        public OverrideVersion(Version ver) {
            super(ver.major(), ver.minor(), ver.vid(), ver.pid());
        }
    }

    public static final Version BAD_VERSION = new Version(-1, -1, (byte) 0, (byte) 0);

    protected int mMajorVersion;
    protected int mMinorVersion;
    protected int mVendorId;
    protected int mProductId;

    public Version(int majorVersion, int minorVersion, int vendorId, int productId) {
        mMajorVersion = majorVersion;
        mMinorVersion = minorVersion;
        mVendorId = vendorId;
        mProductId = productId;
    }

    public int major() {
        return mMajorVersion;
    }

    public int minor() {
        return mMinorVersion;
    }

    public int vid() {
        return mVendorId;
    }

    public int pid() {
        return mProductId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version)) {
            return false;
        }
        Version v = (Version) o;
        if (mVendorId != v.mVendorId || mProductId != v.mProductId) {
            return false;
        }
        return v.mMajorVersion == mMajorVersion && v.mMinorVersion == mMinorVersion;
    }

    @Override
    public int compareTo(Version version) {
        if (this instanceof OverrideVersion && !(version instanceof OverrideVersion)) {
            return 1;
        }
        if (!(this instanceof OverrideVersion) && version instanceof OverrideVersion) {
            return -1;
        }
        if (mVendorId != version.mVendorId) {
            return mVendorId - version.mVendorId;
        }
        if (mProductId != version.mProductId) {
            return mProductId - version.mProductId;
        }
        final int major = mMajorVersion - version.mMajorVersion;
        if (major != 0) {
            return major;
        }
        return mMinorVersion - version.mMinorVersion;
    }

    public String toVersionString() {
        String major = String.valueOf(mMajorVersion);
        String minor = String.valueOf(mMinorVersion);
        return major + "." + minor;
    }

    @Override
    public String toString() {
        return String.format(
                "%02X.%02X (%02X:%02X)", mMajorVersion, mMinorVersion, mVendorId, mProductId);
    }
}
