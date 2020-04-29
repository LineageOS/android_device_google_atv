// Copyright 2020 Google Inc. All Rights Reserved.

#ifndef DEVICE_GOOGLE_ATV_AUDIO_PROXY_SERVICE_AUDIO_PROXY_DEVICES_MANAGER_H_
#define DEVICE_GOOGLE_ATV_AUDIO_PROXY_SERVICE_AUDIO_PROXY_DEVICES_MANAGER_H_

#include PATH(device/google/atv/audio_proxy/FILE_VERSION/IAudioProxyDevicesManager.h)

using ::android::sp;
using ::android::hardware::hidl_string;
using ::android::hardware::Return;
using ::device::google::atv::audio_proxy::CPP_VERSION::IAudioProxyDevicesManager;
using ::device::google::atv::audio_proxy::CPP_VERSION::IBusDevice;

namespace audio_proxy {
namespace service {

class AudioProxyDevicesManagerImpl : public IAudioProxyDevicesManager {
 public:
  AudioProxyDevicesManagerImpl();
  ~AudioProxyDevicesManagerImpl() override;

  Return<bool> registerDevice(const hidl_string& address,
                              const sp<IBusDevice>& device) override;
};

}  // namespace service
}  // namespace audio_proxy

#endif  // DEVICE_GOOGLE_ATV_AUDIO_PROXY_SERVICE_AUDIO_PROXY_DEVICES_MANAGER_H_
