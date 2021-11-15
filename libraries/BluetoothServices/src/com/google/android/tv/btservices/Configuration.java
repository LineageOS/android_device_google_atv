// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices;

import android.content.Context;
import android.content.res.Resources;

public final class Configuration {

    private static final Object LOCK  = new Object();
    private static Configuration mInst;

    private Resources mResources;

    public static Configuration get(Context context) {
        synchronized(LOCK) {
            if (mInst != null) {
              return mInst;
            }
            mInst = new Configuration(context);
            return mInst;
        }
    }

    private Configuration(Context context) {
        mResources = context.getResources();
    }

    public boolean isEnabled(int config) {
        return mResources.getBoolean(config);
    }
}
