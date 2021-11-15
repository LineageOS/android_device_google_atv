package com.google.android.tv.btservices;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.Display;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PowerUtils {

    private static final String TAG = "Atv.PowerUtils";
    private static final boolean DEBUG = false;

    public static boolean isCecControlEnabled(Context context) {
        HdmiControlManager hdmiControlManager = context.getSystemService(HdmiControlManager.class);
        return hdmiControlManager.getHdmiCecEnabled()
                == HdmiControlManager.HDMI_CEC_CONTROL_ENABLED;
    }

    public static void enableCecControl(Context context, boolean enable) {
        HdmiControlManager hdmiControlManager = context.getSystemService(HdmiControlManager.class);
        hdmiControlManager.setHdmiCecEnabled(enable
                ? HdmiControlManager.HDMI_CEC_CONTROL_ENABLED
                : HdmiControlManager.HDMI_CEC_CONTROL_DISABLED);
    }
}
