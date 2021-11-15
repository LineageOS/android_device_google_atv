package com.google.android.tv.btservices;

import android.content.Context;
import android.hardware.hdmi.HdmiControlManager;

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
