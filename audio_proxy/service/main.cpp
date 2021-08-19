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

#include <android-base/logging.h>
#include <android-base/parseint.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <getopt.h>
#include <hidl/HidlTransportSupport.h>

#include <optional>

#include "AudioProxyError.h"
#include "AudioProxyImpl.h"
#include "DevicesFactoryImpl.h"

using android::sp;
using android::status_t;

using namespace audio_proxy::service;

namespace {

std::optional<ServiceConfig> parseServiceConfigFromCommandLine(int argc,
                                                               char** argv) {
  ServiceConfig config;
  static option options[] = {
      {"service_name", required_argument, nullptr, 's'},
      {"buffer_size_ms", required_argument, nullptr, 'b'},
      {"latency_ms", required_argument, nullptr, 'l'},
      {nullptr, 0, nullptr, 0},
  };

  int val = 0;
  while ((val = getopt_long(argc, argv, "s:b:l:", options, nullptr)) != -1) {
    switch (val) {
      case 's':
        config.name = optarg;
        break;
      case 'b':
        if (!android::base::ParseUint(optarg, &config.bufferSizeMs)) {
          return std::nullopt;
        }
        break;
      case 'l':
        if (!android::base::ParseUint(optarg, &config.latencyMs)) {
          return std::nullopt;
        }
        break;

      default:
        break;
    }
  }

  if (config.name.empty() || config.bufferSizeMs == 0 ||
      config.latencyMs == 0) {
    return std::nullopt;
  }

  return config;
}

}  // namespace

int main(int argc, char** argv) {
  auto config = parseServiceConfigFromCommandLine(argc, argv);
  if (!config) {
    return ERROR_INVALID_ARGS;
  }

  // Config thread pool.
  ABinderProcess_setThreadPoolMaxThreadCount(1);
  android::hardware::configureRpcThreadpool(1, false /* callerWillJoin */);

  // Register AudioProxy service.
  auto audioProxy = ndk::SharedRefBase::make<AudioProxyImpl>();
  const std::string audioProxyName =
      std::string(AudioProxyImpl::descriptor) + "/" + config->name;

  binder_status_t binder_status = AServiceManager_addService(
      audioProxy->asBinder().get(), audioProxyName.c_str());
  if (binder_status != STATUS_OK) {
    LOG(ERROR) << "Failed to start " << config->name
               << " AudioProxy service, status " << binder_status;
    return ERROR_AIDL_FAILURE;
  }

  // Register AudioProxy audio HAL.
  auto devicesFactory =
      sp<DevicesFactoryImpl>::make(audioProxy->getBusStreamProvider(), *config);
  status_t status = devicesFactory->registerAsService(config->name);
  if (status != android::OK) {
    LOG(ERROR) << "Failed to start " << config->name << " audio HAL, status "
               << status;
    return ERROR_HIDL_FAILURE;
  }

  ABinderProcess_joinThreadPool();

  // `ABinderProcess_joinThreadpool` should never return. Return -2 here for
  // unexpected process exit.
  return ERROR_UNEXPECTED;
}
