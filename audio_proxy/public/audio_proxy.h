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

#ifndef DEVICE_GOOGLE_ATV_AUDIO_PROXY_PUBLIC_AUDIO_PROXY_H_
#define DEVICE_GOOGLE_ATV_AUDIO_PROXY_PUBLIC_AUDIO_PROXY_H_

#include <stdint.h>
#include <sys/types.h>
#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif

// audio proxy allows the application to implement an audio HAL. It contains two
// components, a client library and a service.
// The client library is defined by this header file. Applications should
// integrate this library to provide audio HAL components. Currently it's only
// IStreamOut.
// The service implements IDevicesFactory and IDevice. It will register itself
// to audio server and forward function calls to client.

// Most of the struct/functions just converts the HIDL definitions into C
// definitions.

// Represents an audio HAL bus device.
struct audio_proxy_device {
  // Returns the unique address of this device.
  const char* (*get_address)(struct audio_proxy_device* device);

  // Points to next version's struct. Implementation should set this field to
  // null if next version struct is not available.
  // This allows library to work with applications integrated with older version
  // header.
  void* extension;
};

typedef struct audio_proxy_device audio_proxy_device_t;

// Provides |device| to the library. It returns 0 on success. This function is
// supposed to be called once per process.
// The service behind this library will register a new audio HAL to the audio
// server, on the first call to the service.
int audio_proxy_register_device(audio_proxy_device_t* device);

#ifdef __cplusplus
}
#endif

#endif  // DEVICE_GOOGLE_ATV_AUDIO_PROXY_PUBLIC_AUDIO_PROXY_H_
