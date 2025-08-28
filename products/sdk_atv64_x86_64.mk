#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
PRODUCT_IS_ATV_SDK := true

$(call inherit-product, device/google/atv/products/aosp_tv_x86_64.mk)

# Overrides
PRODUCT_BRAND := Android
PRODUCT_NAME := sdk_atv_x86_64
PRODUCT_DEVICE := emu64x
