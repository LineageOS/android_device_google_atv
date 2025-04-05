#
# Copyright (C) 2020 The Android Open Source Project
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
# This makefile contains the product partition contents for
# a generic TV device.
$(call inherit-product, $(SRC_TARGET_DIR)/product/media_product.mk)

PRODUCT_PACKAGES += \
    TvNetworkStackOverlay \
    TvFrameworkOverlay \
    TvSettingsProviderOverlay \
    TvWifiOverlay \
    SettingsIntelligence

# Override com.android.* overlays with com.google.android.* overlays for mainline
ifeq ($(PRODUCT_IS_ATV_MAINLINE), true)
PRODUCT_PACKAGES += \
    TvWifiOverlayGoogle
endif

PRODUCT_COPY_FILES += \
    device/google/atv/atv-component-overrides.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/sysconfig/atv-component-overrides.xml

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.gamepad.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/android.hardware.gamepad.xml

# Copy .kl file for generic voice remotes
PRODUCT_PACKAGES += atv_generic_keylayout
$(call soong_config_set_bool,atv_keylayouts,use_atv_generic_keylayout,true)

# Too many tombstones can cause bugreports to grow too large to be uploaded.
PRODUCT_PRODUCT_PROPERTIES += \
    tombstoned.max_tombstone_count?=10

# Limit persistent logs to 10MB
PRODUCT_PRODUCT_PROPERTIES += \
    logd.logpersistd.size=5 \
    logd.logpersistd.rotate_kbytes=2048
