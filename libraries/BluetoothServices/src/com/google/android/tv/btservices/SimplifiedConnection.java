// Copyright 2019 Google Inc. All Rights Reserved.

package com.google.android.tv.btservices;

import android.content.ComponentName;
import android.content.ServiceConnection;

public abstract class SimplifiedConnection implements ServiceConnection {

    protected abstract void cleanUp();

    @Override
    public void onServiceDisconnected(ComponentName name) {
        cleanUp();
    }

    @Override
    public void onBindingDied(ComponentName name) {
        cleanUp();
    }
}
