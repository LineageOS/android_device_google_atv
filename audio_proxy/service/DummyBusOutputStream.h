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

#include "BusOutputStream.h"

namespace audio_proxy::service {

// Impl of BusOutputStream which blocks the write, a.k.a, it always has
// 0 available write space.
class DummyBusOutputStream : public BusOutputStream {
 public:
  DummyBusOutputStream(const std::string& address,
                       const AidlAudioConfig& config, int32_t flags);
  ~DummyBusOutputStream() override;

  bool standby() override;
  bool pause() override;
  bool resume() override;
  bool drain(AidlAudioDrain drain) override;
  bool flush() override;
  bool close() override;
  bool setVolume(float left, float right) override;

  size_t availableToWrite() override;
  AidlWriteStatus writeRingBuffer(const uint8_t* firstMem, size_t firstLength,
                                  const uint8_t* secondMem,
                                  size_t secondLength) override;

 protected:
  bool prepareForWritingImpl(uint32_t frameSize, uint32_t frameCount) override;
};

}  // namespace audio_proxy::service