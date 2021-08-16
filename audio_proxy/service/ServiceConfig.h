// Copyright (C) 2021 The Android Open Source Project
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

#pragma once

#include <stdint.h>

#include <string>

namespace audio_proxy::service {

// Global configurations for the audio HAL service and AudioProxy service.
struct ServiceConfig {
  // Name of the service. It will be used to identify the audio HAL service and
  // AudioProxy service.
  std::string name;

  // Buffer size in milliseconds, as defined by IStream::getBufferSize.
  uint32_t bufferSizeMs = 0;

  // Latency in milliseconds, as defined by IStreamOut::getLatency.
  uint32_t latencyMs = 0;
};

}  // namespace audio_proxy::service