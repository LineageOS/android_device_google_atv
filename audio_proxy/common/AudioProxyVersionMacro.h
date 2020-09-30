/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma once

#if !defined(AUDIO_PROXY_MINOR_VERSION)
#error "AUDIO_PROXY_MINOR_VERSION must be defined."
#endif  // !defined(AUDIO_PROXY_MINOR_VERSION)

/** The directory name of the version: <major>.<minor> */
#define AUDIO_PROXY_FILE_VERSION \
  EXPAND_CONCAT_3(MAJOR_VERSION, ., AUDIO_PROXY_MINOR_VERSION)

/** The c++ namespace of the version: V<major>_<minor> */
#define AUDIO_PROXY_CPP_VERSION \
  EXPAND_CONCAT_4(V, MAJOR_VERSION, _, AUDIO_PROXY_MINOR_VERSION)