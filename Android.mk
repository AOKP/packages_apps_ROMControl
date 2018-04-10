LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v14-preference \
    android-support-v13 \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-cardview \
    android-support-v7-appcompat \
    android-support-v4 \
    android-support-design \
    AndroidAsync \
    gson \
    jsr305 \
    org.lineageos.platform.internal

LOCAL_SRC_FILES := $(call all-subdir-java-files, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v14/preference/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/appcompat/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/cardview/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/preference/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/recyclerview/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/design/res

LOCAL_PACKAGE_NAME := ROMControl
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

include frameworks/opt/setupwizard/navigationbar/common.mk
include frameworks/opt/setupwizard/library/common.mk
include frameworks/base/packages/SettingsLib/common.mk

LOCAL_JAVA_LIBRARIES += org.lineageos.hardware
include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
