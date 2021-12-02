#
# Copyright (C) 2022 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

PRODUCT_IS_ATV_SDK := true

# ATV SDK is not designed to have a camera by default
PRODUCT_SUPPORTS_CAMERA ?= false

QEMU_USE_SYSTEM_EXT_PARTITIONS := true

# This is a build configuration for a full-featured build of the
# Open-Source part of the tree. It's geared toward a US-centric
# build quite specifically for the emulator, and might not be
# entirely appropriate to inherit from for on-device configurations.

PRODUCT_ENFORCE_ARTIFACT_PATH_REQUIREMENTS := relaxed


$(call inherit-product, device/google/atv/products/aosp_tv_arm64.mk)

#
# All components inherited here go to system_ext image
#
$(call inherit-product, device/google/atv/products/atv_vendor.mk)

#
# All components inherited here go to vendor or vendor_boot image
#

# ATV SDK is not designed to have a GNSS-receiver by default
EMULATOR_VENDOR_NO_GNSS := true

DEVICE_PACKAGE_OVERLAYS := \
    device/generic/goldfish/overlay \
    device/google/atv/sdk_overlay \
    development/sdk_overlay

PRODUCT_COPY_FILES += \
    device/generic/goldfish/data/etc/config.ini.tv:config.ini

PRODUCT_COPY_FILES += \
    device/generic/goldfish/data/etc/apns-conf.xml:$(TARGET_COPY_OUT_VENDOR)/etc/apns-conf.xml \
    device/generic/goldfish/camera/media_codecs.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_audio.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_audio.xml \
    frameworks/native/data/etc/android.hardware.ethernet.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.ethernet.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_telephony.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_telephony.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_tv.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_tv.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_video.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_video.xml \
    hardware/libhardware_legacy/audio/audio_policy.conf:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy.conf

# Exclude all non-default hardware features on ATV SDK.
# All default supported features are defined via device/google/atv/permissions/tv_core_hardware.xml.
PRODUCT_COPY_FILES += \
    device/google/atv/permissions/tv_sdk_excluded_core_hardware.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/tv_sdk_excluded_core_hardware.xml


# keep this apk for sdk targets for now
PRODUCT_PACKAGES += \
    EmulatorSmokeTests

# Overrides
PRODUCT_BRAND := Android
PRODUCT_NAME := sdk_atv_arm64
PRODUCT_DEVICE := emulator64_arm64

PRODUCT_PRODUCT_PROPERTIES += \
    ro.oem.key1=ATV00100020
