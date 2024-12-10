#
# Copyright (C) 2020 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Sets Android TV Low RAM recommended default product options.

# Set lowram options and enable traced by default
PRODUCT_VENDOR_PROPERTIES += ro.config.low_ram=true

# Speed profile services and wifi-service to reduce RAM and storage.
PRODUCT_SYSTEM_SERVER_COMPILER_FILTER := speed-profile

# Always preopt extracted APKs to prevent extracting out of the APK for gms
# modules.
PRODUCT_ALWAYS_PREOPT_EXTRACTED_APK := true

# Use TV specific preloaded classes for the boot classpath, determines which
# methods from the boot classpath get optimized, which class is included in
# the boot .art image, and how the corresponding DEX files are laid out.
PRODUCT_COPY_FILES += device/google/atv/products/lowram_boot_profiles/preloaded-classes:system/etc/preloaded-classes

# Use TV specific profile for the boot classpath, determines which methods
# from the boot classpath get optimized, which class is included in the boot
# .art image, and how the corresponding DEX files are laid out.
PRODUCT_USE_PROFILE_FOR_BOOT_IMAGE := true
PRODUCT_DEX_PREOPT_BOOT_IMAGE_PROFILE_LOCATION := device/google/atv/products/lowram_boot_profiles/boot-image-profile.txt

# Add the system properties.
TARGET_SYSTEM_PROP += \
    build/make/target/board/go_defaults_common.prop

# Dedupe VNDK libraries with identical core variants.
TARGET_VNDK_USE_CORE_VARIANT := true

# Use the low memory allocator outside of eng builds to save RSS.
ifeq (,$(filter eng, $(TARGET_BUILD_VARIANT)))
  MALLOC_SVELTE := true
endif

# Overlay for lowram
PRODUCT_PACKAGES += TvLowRamOverlay

# Disable camera by default
PRODUCT_SUPPORTS_CAMERA ?= false
