// Copyright 2020 Google Inc. All Rights Reserved.

#include "AudioProxyDevicesManagerImpl.h"

namespace audio_proxy {
namespace service {

AudioProxyDevicesManagerImpl::AudioProxyDevicesManagerImpl() = default;
AudioProxyDevicesManagerImpl::~AudioProxyDevicesManagerImpl() = default;

Return<bool> AudioProxyDevicesManagerImpl::registerDevice(
    const hidl_string& address, const sp<IBusDevice>& device) {
  return false;
}

}  // namespace service
}  // namespace audio_proxy
