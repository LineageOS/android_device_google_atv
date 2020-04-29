// Copyright 2020 Google Inc. All Rights Reserved.

#define LOG_TAG "audio_proxy_service"

#include <hidl/HidlTransportSupport.h>
#include <utils/Log.h>

#include "AudioProxyDevicesManagerImpl.h"

using ::android::sp;
using ::android::status_t;

int main(int argc, char** argv) {
  android::hardware::configureRpcThreadpool(1, true /* callerWillJoin */);

  sp<audio_proxy::service::AudioProxyDevicesManagerImpl> manager =
      new audio_proxy::service::AudioProxyDevicesManagerImpl();
  status_t status = manager->registerAsService();
  if (status != android::OK) {
    ALOGE("fail to register devices factory manager: %x", status);
    return -1;
  }

  ::android::hardware::joinRpcThreadpool();

  // `joinRpcThreadpool` should never return. Return -2 here for unexpected
  // process exit.
  return -2;
}
