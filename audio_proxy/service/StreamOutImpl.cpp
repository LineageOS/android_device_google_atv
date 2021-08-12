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

#include "StreamOutImpl.h"

#include <android-base/logging.h>
#include <inttypes.h>
#include <system/audio.h>
#include <time.h>
#include <utils/Log.h>

#include <cstring>

#include "AidlTypes.h"
#include "BusOutputStream.h"

using android::status_t;

namespace audio_proxy::service {

namespace {

// 1GB
constexpr uint32_t kMaxBufferSize = 1 << 30;
constexpr uint32_t kDefaultLatencyMs = 40;

uint64_t calcFrameSize(const AudioConfig& config) {
  audio_format_t format = static_cast<audio_format_t>(config.format);

  if (!audio_has_proportional_frames(format)) {
    return sizeof(int8_t);
  }

  size_t channelSampleSize = audio_bytes_per_sample(format);
  return audio_channel_count_from_out_mask(
             static_cast<audio_channel_mask_t>(config.channelMask)) *
         channelSampleSize;
}

AudioConfig fromAidlAudioConfig(const AidlAudioConfig& aidlConfig) {
  AudioConfig config;
  config.format = static_cast<AudioFormat>(aidlConfig.format);
  config.sampleRateHz = static_cast<uint32_t>(aidlConfig.sampleRateHz);
  config.channelMask =
      static_cast<hidl_bitfield<AudioChannelMask>>(aidlConfig.channelMask);

  return config;
}

}  // namespace

StreamOutImpl::StreamOutImpl(std::shared_ptr<BusOutputStream> stream)
    : mStream(std::move(stream)),
      mConfig(fromAidlAudioConfig(mStream->getConfig())) {}

StreamOutImpl::~StreamOutImpl() = default;

Return<uint64_t> StreamOutImpl::getFrameSize() {
  return calcFrameSize(mConfig);
}

Return<uint64_t> StreamOutImpl::getFrameCount() {
  return 20 * mConfig.sampleRateHz / 1000;
}

Return<uint64_t> StreamOutImpl::getBufferSize() {
  // TODO(yucliu): The buffer size should be provided by command line args.
  return 20 * mConfig.sampleRateHz * calcFrameSize(mConfig) / 1000;
}

Return<uint32_t> StreamOutImpl::getSampleRate() { return mConfig.sampleRateHz; }

Return<void> StreamOutImpl::getSupportedSampleRates(
    AudioFormat format, getSupportedSampleRates_cb _hidl_cb) {
  _hidl_cb(Result::NOT_SUPPORTED, {});
  return Void();
}

Return<void> StreamOutImpl::getSupportedChannelMasks(
    AudioFormat format, getSupportedChannelMasks_cb _hidl_cb) {
  _hidl_cb(Result::NOT_SUPPORTED, {});
  return Void();
}

Return<Result> StreamOutImpl::setSampleRate(uint32_t sampleRateHz) {
  return Result::NOT_SUPPORTED;
}

Return<hidl_bitfield<AudioChannelMask>> StreamOutImpl::getChannelMask() {
  return mConfig.channelMask;
}

Return<Result> StreamOutImpl::setChannelMask(
    hidl_bitfield<AudioChannelMask> mask) {
  return Result::NOT_SUPPORTED;
}

Return<AudioFormat> StreamOutImpl::getFormat() { return mConfig.format; }

Return<void> StreamOutImpl::getSupportedFormats(
    getSupportedFormats_cb _hidl_cb) {
  _hidl_cb({});
  return Void();
}

Return<Result> StreamOutImpl::setFormat(AudioFormat format) {
  return Result::NOT_SUPPORTED;
}

Return<void> StreamOutImpl::getAudioProperties(getAudioProperties_cb _hidl_cb) {
  _hidl_cb(mConfig.sampleRateHz, mConfig.channelMask, mConfig.format);
  return Void();
}

Return<Result> StreamOutImpl::addEffect(uint64_t effectId) {
  return Result::NOT_SUPPORTED;
}

Return<Result> StreamOutImpl::removeEffect(uint64_t effectId) {
  return Result::NOT_SUPPORTED;
}

Return<Result> StreamOutImpl::standby() {
  return mStream->standby() ? Result::OK : Result::INVALID_STATE;
}

Return<void> StreamOutImpl::getDevices(getDevices_cb _hidl_cb) {
  _hidl_cb(Result::NOT_SUPPORTED, {});
  return Void();
}

Return<Result> StreamOutImpl::setDevices(
    const hidl_vec<DeviceAddress>& devices) {
  return Result::NOT_SUPPORTED;
}

Return<void> StreamOutImpl::getParameters(
    const hidl_vec<ParameterValue>& context, const hidl_vec<hidl_string>& keys,
    getParameters_cb _hidl_cb) {
  _hidl_cb(Result::OK, {});
  return Void();
}

Return<Result> StreamOutImpl::setParameters(
    const hidl_vec<ParameterValue>& context,
    const hidl_vec<ParameterValue>& parameters) {
  return Result::OK;
}

Return<Result> StreamOutImpl::setHwAvSync(uint32_t hwAvSync) {
  return Result::NOT_SUPPORTED;
}

Return<Result> StreamOutImpl::close() {
  return mStream->close() ? Result::OK : Result::INVALID_STATE;
}

Return<uint32_t> StreamOutImpl::getLatency() {
  // TODO(yucliu): If no audio data is written into client, use the default
  // latency from command line args. Otherwise calculate the value from
  // AidlWriteStatus returned by mStream.writeRingBuffer.
  return kDefaultLatencyMs;
}

Return<Result> StreamOutImpl::setVolume(float left, float right) {
  return mStream->setVolume(left, right) ? Result::OK : Result::INVALID_STATE;
}

Return<void> StreamOutImpl::prepareForWriting(uint32_t frameSize,
                                              uint32_t framesCount,
                                              prepareForWriting_cb _hidl_cb) {
  ThreadInfo threadInfo = {0, 0};

  // Wrap the _hidl_cb to return an error
  auto sendError = [&threadInfo, &_hidl_cb](Result result) -> Return<void> {
    _hidl_cb(result, CommandMQ::Descriptor(), DataMQ::Descriptor(),
             StatusMQ::Descriptor(), threadInfo);
    return Void();
  };

  // TODO(yucliu): Create a thread to read data from FMQ and write the data into
  // mStream.
  return sendError(Result::INVALID_STATE);
}

Return<void> StreamOutImpl::getRenderPosition(getRenderPosition_cb _hidl_cb) {
  // TODO(yucliu): Render position can be calculated by the AidlWriteStatus
  // returned by the mStream.writeRingBuffer.
  _hidl_cb(Result::NOT_SUPPORTED, 0);
  return Void();
}

Return<void> StreamOutImpl::getNextWriteTimestamp(
    getNextWriteTimestamp_cb _hidl_cb) {
  _hidl_cb(Result::NOT_SUPPORTED, 0);
  return Void();
}

Return<Result> StreamOutImpl::setCallback(
    const sp<IStreamOutCallback>& callback) {
  return Result::NOT_SUPPORTED;
}

Return<Result> StreamOutImpl::clearCallback() { return Result::NOT_SUPPORTED; }

Return<void> StreamOutImpl::supportsPauseAndResume(
    supportsPauseAndResume_cb _hidl_cb) {
  _hidl_cb(true, true);
  return Void();
}

Return<Result> StreamOutImpl::pause() {
  return mStream->pause() ? Result::OK : Result::INVALID_STATE;
}

Return<Result> StreamOutImpl::resume() {
  return mStream->resume() ? Result::OK : Result::INVALID_STATE;
}

Return<bool> StreamOutImpl::supportsDrain() { return true; }

Return<Result> StreamOutImpl::drain(AudioDrain type) {
  return mStream->drain(static_cast<AidlAudioDrain>(type))
             ? Result::OK
             : Result::INVALID_STATE;
}

Return<Result> StreamOutImpl::flush() {
  return mStream->flush() ? Result::OK : Result::INVALID_STATE;
}

Return<void> StreamOutImpl::getPresentationPosition(
    getPresentationPosition_cb _hidl_cb) {
  // TODO(yucliu): Presentation position can be calculated by the
  // AidlWriteStatus returned by the mStream.writeRingBuffer.
  _hidl_cb(Result::NOT_SUPPORTED, 0, {});
  return Void();
}

Return<Result> StreamOutImpl::start() { return Result::NOT_SUPPORTED; }

Return<Result> StreamOutImpl::stop() { return Result::NOT_SUPPORTED; }

Return<void> StreamOutImpl::createMmapBuffer(int32_t minSizeFrames,
                                             createMmapBuffer_cb _hidl_cb) {
  _hidl_cb(Result::NOT_SUPPORTED, MmapBufferInfo());
  return Void();
}

Return<void> StreamOutImpl::getMmapPosition(getMmapPosition_cb _hidl_cb) {
  _hidl_cb(Result::NOT_SUPPORTED, MmapPosition());
  return Void();
}

Return<void> StreamOutImpl::updateSourceMetadata(
    const SourceMetadata& sourceMetadata) {
  return Void();
}

Return<Result> StreamOutImpl::selectPresentation(int32_t presentationId,
                                                 int32_t programId) {
  return Result::NOT_SUPPORTED;
}

std::shared_ptr<BusOutputStream> StreamOutImpl::getOutputStream() {
  return mStream;
}

void StreamOutImpl::updateOutputStream(
    std::shared_ptr<BusOutputStream> stream) {
  DCHECK(stream);
  DCHECK(mStream);
  if (stream->getConfig() != mStream->getConfig()) {
    LOG(ERROR) << "New stream's config doesn't match the old stream's config.";
    return;
  }

  // TODO(yucliu): Call mStream.prepareForWriting if audioserver starts to write
  // data.

  mStream = std::move(stream);
}

}  // namespace audio_proxy::service
