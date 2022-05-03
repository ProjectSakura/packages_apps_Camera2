LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.legacy_legacy-support-v13 \
    androidx.legacy_legacy-support-v4 \
    androidx.core_core

LOCAL_STATIC_JAVA_LIBRARIES := android-ex-camera2-portability
LOCAL_STATIC_JAVA_LIBRARIES += xmp_toolkit
LOCAL_STATIC_JAVA_LIBRARIES += glide
LOCAL_STATIC_JAVA_LIBRARIES += guava
LOCAL_STATIC_JAVA_LIBRARIES += jsr305
LOCAL_STATIC_JAVA_LIBRARIES += camera2-zxing-core

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, src_pd)
LOCAL_SRC_FILES += $(call all-java-files-under, src_pd_gcam)
LOCAL_SRC_FILES += $(call all-java-files-under, quickReader/src)

LOCAL_RESOURCE_DIR += \
	$(LOCAL_PATH)/res \
	$(LOCAL_PATH)/res_p \
	$(LOCAL_PATH)/quickReader/res

include $(LOCAL_PATH)/version.mk

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --version-name "$(version_name_package)" \
        --version-code $(version_code_package) \
        --extra-packages me.dm7.barcodescanner.core \
        --extra-packages me.dm7.barcodescanner.zxing \

LOCAL_STATIC_JAVA_AAR_LIBRARIES += \
    camera2-qreader-core \
    camera2-qreader-zxing

LOCAL_USE_AAPT2 := true

LOCAL_PACKAGE_NAME := Camera2
LOCAL_CERTIFICATE := platform

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PRIVILEGED_MODULE := true

LOCAL_SYSTEM_EXT_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# Guava uses deprecated org.apache.http.legacy classes.
LOCAL_JAVA_LIBRARIES += org.apache.http.legacy

LOCAL_JNI_SHARED_LIBRARIES := libjni_tinyplanet libjni_jpegutil

LOCAL_REQUIRED_MODULES := privapp_whitelist_com.android.camera2.xml

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := privapp_whitelist_com.android.camera2.xml
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_SYSTEM_EXT_ETC)/permissions
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))
