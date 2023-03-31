#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_isw_gateway_TransactionProcessorWrapper_getIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.10");
}

JNIEXPORT jstring JNICALL
Java_com_isw_gateway_TransactionProcessorWrapper_getPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "55533");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_Utility_getDefaultIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.73");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_Utility_getDefaultPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "5043");
}