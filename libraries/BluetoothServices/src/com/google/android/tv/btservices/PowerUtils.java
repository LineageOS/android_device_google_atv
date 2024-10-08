/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public static boolean isEnabledGoToSleepOnActiveSourceLost(Context context) {
        HdmiControlManager hdmiControlManager = context.getSystemService(HdmiControlManager.class);
        return hdmiControlManager.getPowerStateChangeOnActiveSourceLost().equals(
            HdmiControlManager.POWER_STATE_CHANGE_ON_ACTIVE_SOURCE_LOST_STANDBY_NOW);
    }

    public static void setPowerStateChangeOnActiveSourceLost(Context context, boolean enable) {
        HdmiControlManager hdmiControlManager = context.getSystemService(HdmiControlManager.class);
        hdmiControlManager.setPowerStateChangeOnActiveSourceLost(enable
            ? HdmiControlManager.POWER_STATE_CHANGE_ON_ACTIVE_SOURCE_LOST_STANDBY_NOW
            : HdmiControlManager.POWER_STATE_CHANGE_ON_ACTIVE_SOURCE_LOST_NONE);
    }

    public static boolean isPlaybackDevice(Context context) {
        HdmiControlManager hdmiControlManager = context.getSystemService(HdmiControlManager.class);
        return hdmiControlManager.getPlaybackClient() != null;
    }
}
