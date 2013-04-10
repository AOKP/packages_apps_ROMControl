LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 android-support-v4

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := ROMControl
LOCAL_CERTIFICATE := platform

# include mGerrit package
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_AAPT_FLAGS := --extra-packages com.jbirdvegas.mgerrit --auto-add-overlay
LOCAL_SRC_FILES += $(LOCAL_PATH)/res $(call all-java-files-under,../../../external/jbirdvegas/mGerrit/src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res $(LOCAL_PATH)/../../../external/jbirdvegas/mGerrit/res
LOCAL_STATIC_JAVA_LIBRARIES += nineoldandroids-2.4.0
include $(BUILD_PACKAGE)

# include java jar used by CardsUI
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := nineoldandroids-2.4.0:../../../external/jbirdvegas/mGerrit/libs/nineoldandroids-2.4.0.jar
include $(BUILD_MULTI_PREBUILT)
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
