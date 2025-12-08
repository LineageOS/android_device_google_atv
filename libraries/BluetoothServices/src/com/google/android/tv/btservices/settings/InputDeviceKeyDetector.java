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

package com.google.android.tv.btservices.settings;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputDevice;

public class InputDeviceKeyDetector {

    public static boolean isCustomButtonPresent(Context context, int keyCode) {
        if (context == null) {
            return false;
        }
        InputManager inputManager = context.getSystemService(InputManager.class);
        if (inputManager == null) {
            return false;
        }
        for (int deviceId : inputManager.getInputDeviceIds()) {
            InputDevice inputDevice = inputManager.getInputDevice(deviceId);
            if (inputDevice != null && !inputDevice.isVirtual() &&
                inputDevice.hasKeys(keyCode)[0]) {
                return true;
            }
        }
        return false;
    }
}
