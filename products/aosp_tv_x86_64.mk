#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Generate source.properties for image target
PRODUCT_SDK_ADDON_SYS_IMG_SOURCE_PROP := \
    device/google/atv/sdk/images_source.prop_template

PRODUCT_USE_DYNAMIC_PARTITIONS := true

# The system image of aosp_tv_x86_64-userdebug is a GSI for the devices with:
# - x86_64 32 bits user space
# - 64 bits binder interface
# - VNDK enforcement
# - compatible property override enabled

#
# All components inherited here go to system image
#
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, device/google/atv/products/atv_generic_system.mk)

# Enable mainline checking for excat this product name
ifeq (aosp_tv_x86_64,$(TARGET_PRODUCT))
PRODUCT_ENFORCE_ARTIFACT_PATH_REQUIREMENTS := relaxed
endif

#
# All components inherited here go to system_ext image
#
$(call inherit-product, device/google/atv/products/atv_system_ext.mk)
# Packages required for ATV GSI
PRODUCT_PACKAGES += \
    TvProvision

#
# All components inherited here go to product image
#
$(call inherit-product, device/google/atv/products/atv_product.mk)
# Packages required for ATV GSI
PRODUCT_PACKAGES += \
    LeanbackIME \
    TvSampleLeanbackLauncher

#
# All components inherited here go to vendor image
#
$(call inherit-product, device/google/atv/products/atv_emulator_vendor.mk)
$(call inherit-product, device/generic/goldfish/board/emu64x/details.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/board/generic_x86_64/device.mk)

ifeq (aosp_tv_x86_64,$(TARGET_PRODUCT))
$(call inherit-product, $(SRC_TARGET_DIR)/product/gsi_release.mk)
endif

PRODUCT_NAME := aosp_tv_x86_64
PRODUCT_DEVICE := generic_x86_64
PRODUCT_BRAND := Android
PRODUCT_MODEL := AOSP TV on x86_64
