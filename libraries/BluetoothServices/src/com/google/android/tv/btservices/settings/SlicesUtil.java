// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;

import androidx.core.graphics.drawable.IconCompat;

import com.android.tv.twopanelsettings.slices.SlicesConstants;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;

import com.google.android.tv.btservices.BluetoothUtils;
import com.google.android.tv.btservices.R;

/**
 * Utility class for slices.
 **/
public final class SlicesUtil {

    static final String AUTHORITY = "com.google.android.tv.btservices.settings.sliceprovider";
    static final String GENERAL_PATH = "general";
    static final String BLUETOOTH_DEVICE_PATH = "device";
    static final String CEC_PATH = "cec";
    static final String EXTRAS_DIRECTION = "extras_direction";
    static final String EXTRAS_SLICE_URI = "extras_slice_uri";
    static final String DIRECTION_BACK = "direction_back";
    static final Uri GENERAL_SLICE_URI =
            Uri.parse("content://" + AUTHORITY + "/" + GENERAL_PATH);
    static final Uri BLUETOOTH_DEVICE_SLICE_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BLUETOOTH_DEVICE_PATH);
    static final Uri CEC_SLICE_URI =
            Uri.parse("content://" + AUTHORITY + "/" + CEC_PATH);
    static final Uri AXEL_SLICE_URI =
            Uri.parse("content://com.google.android.tv.axel.sliceprovider/main");

    static String getDeviceAddr(Uri uri) {
        if (uri.getPathSegments().size() >= 2) {
            return uri.getPathSegments().get(1).split(" ")[0];
        }
        return null;
    }

    static boolean isGeneralPath(Uri uri) {
        return GENERAL_PATH.equals(getFirstSegment(uri));
    }

    static boolean isBluetoothDevicePath(Uri uri) {
        return BLUETOOTH_DEVICE_PATH.equals(getFirstSegment(uri));
    }

    static boolean isCecPath(Uri uri) {
        return CEC_PATH.equals(getFirstSegment(uri));
    }

    static Uri getDeviceUri(String deviceAddr, String aliasName) {
        return Uri.withAppendedPath(
                BLUETOOTH_DEVICE_SLICE_URI, deviceAddr + " " + aliasName);
    }

    private static String getFirstSegment(Uri uri) {
        if (uri.getPathSegments().size() > 0) {
            return uri.getPathSegments().get(0);
        }
        return null;
    }

    static void notifyToGoBack(Context context, Uri uri) {
        Uri appendedUri = uri
                .buildUpon().path("/" + SlicesConstants.PATH_STATUS)
                .appendQueryParameter(SlicesConstants.PARAMETER_URI, uri.toString())
                .appendQueryParameter(SlicesConstants.PARAMETER_DIRECTION, SlicesConstants.BACKWARD)
                .build();
        context.getContentResolver().notifyChange(appendedUri, null);
    }
}
