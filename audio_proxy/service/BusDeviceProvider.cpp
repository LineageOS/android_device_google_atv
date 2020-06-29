// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "BusDeviceProvider.h"
#include "DummyBusDevice.h"

#include <algorithm>

namespace audio_proxy {
namespace service {

class BusDeviceProvider::DeathRecipient : public hidl_death_recipient {
 public:
  explicit DeathRecipient(BusDeviceProvider& owner) : mOwner(owner) {}
  ~DeathRecipient() override = default;

  void serviceDied(
      uint64_t token,
      const android::wp<::android::hidl::base::V1_0::IBase>& who) override {
    mOwner.removeByToken(token);
  }

 private:
  BusDeviceProvider& mOwner;
};

BusDeviceProvider::BusDeviceProvider()
    : mDeathRecipient(new DeathRecipient(*this)) {}
BusDeviceProvider::~BusDeviceProvider() = default;

bool BusDeviceProvider::add(const hidl_string& address, sp<IBusDevice> device) {
  std::lock_guard<std::mutex> guard(mDevicesLock);
  auto it = std::find_if(mBusDevices.begin(), mBusDevices.end(),
                         [&address](const BusDeviceHolder& holder) {
                           return holder.address == address;
                         });

  if (it != mBusDevices.end()) {
    return false;
  }

  uint64_t token = mNextToken++;

  mBusDevices.push_back({device, address, token});

  device->linkToDeath(mDeathRecipient, token);

  return true;
}

sp<IBusDevice> BusDeviceProvider::get(const hidl_string& address) {
  std::lock_guard<std::mutex> guard(mDevicesLock);
  auto it = std::find_if(mBusDevices.begin(), mBusDevices.end(),
                         [&address](const BusDeviceHolder& holder) {
                           return holder.address == address;
                         });

  if (it == mBusDevices.end()) {
    // When AudioPolicyManager first opens this HAL, it iterates through the
    // devices and quickly opens and closes the first device (as specified in
    // the audio configuration .xml). However, it is possible that the first
    // device has not registered with the audio proxy HAL yet. In this case, we
    // will return a dummy device, which is going to create a dummy output
    // stream. Since this HAL only supports direct outputs, the dummy output
    // will be immediately closed until it is reopened on use -- and by that
    // time the actual device must have registered itself.
    return new DummyBusDevice();
  }

  return it->device;
}

void BusDeviceProvider::removeAll() {
  std::lock_guard<std::mutex> guard(mDevicesLock);
  mBusDevices.clear();
}

void BusDeviceProvider::removeByToken(uint64_t token) {
  std::lock_guard<std::mutex> guard(mDevicesLock);
  mBusDevices.erase(std::find_if(mBusDevices.begin(), mBusDevices.end(),
                                 [token](const BusDeviceHolder& holder) {
                                   return holder.token == token;
                                 }));
}

}  // namespace service
}  // namespace audio_proxy
