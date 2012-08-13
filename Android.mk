LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle
LOCAL_STATIC_JAVA_LIBRARIES := guava android-support-v4 android-support-v13

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := ROMControl
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
