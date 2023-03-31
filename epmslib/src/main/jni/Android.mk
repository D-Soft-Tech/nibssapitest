LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := module-params
LOCAL_SRC_FILES := module-params.c
include $(BUILD_SHARED_LIBRARY)