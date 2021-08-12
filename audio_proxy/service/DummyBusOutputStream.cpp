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

#include "DummyBusOutputStream.h"

#include <android-base/logging.h>

namespace audio_proxy::service {

DummyBusOutputStream::DummyBusOutputStream(const std::string& address,
                                           const AidlAudioConfig& config,
                                           int32_t flags)
    : BusOutputStream(address, config, flags) {}
DummyBusOutputStream::~DummyBusOutputStream() = default;

bool DummyBusOutputStream::standby() { return true; }
bool DummyBusOutputStream::pause() { return true; }
bool DummyBusOutputStream::resume() { return true; }
bool DummyBusOutputStream::drain(AidlAudioDrain drain) { return true; }
bool DummyBusOutputStream::flush() { return true; }
bool DummyBusOutputStream::close() { return true; }
bool DummyBusOutputStream::setVolume(float left, float right) { return true; }

size_t DummyBusOutputStream::availableToWrite() { return 0; }

AidlWriteStatus DummyBusOutputStream::writeRingBuffer(const uint8_t* firstMem,
                                                      size_t firstLength,
                                                      const uint8_t* secondMem,
                                                      size_t secondLength) {
  return {};
}

bool DummyBusOutputStream::prepareForWritingImpl(uint32_t frameSize,
                                                 uint32_t frameCount) {
  return true;
}

}  // namespace audio_proxy::service