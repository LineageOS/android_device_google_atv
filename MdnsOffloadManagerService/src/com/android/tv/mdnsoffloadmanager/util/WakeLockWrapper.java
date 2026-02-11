package com.android.tv.mdnsoffloadmanager.util;

import android.os.PowerManager;
import android.util.Log;

/**
 * Wrapper around {@link android.os.PowerManager.WakeLock} for testing purposes.
 */
public class WakeLockWrapper {
    private static final String TAG = WakeLockWrapper.class.getSimpleName();
    private final PowerManager.WakeLock mLock;

    public WakeLockWrapper(PowerManager.WakeLock lock) {
        this.mLock = lock;
    }

    /**
     * @see PowerManager.WakeLock#acquire()
     */
    public void acquire(long timeout) {
        mLock.acquire(timeout);
    }

    /**
     * @see PowerManager.WakeLock#release()
     */
    public void release() {
        try {
            mLock.release();
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to release wakelock", e);
        }
    }
}
