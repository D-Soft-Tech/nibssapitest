#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_network_StormApiClient_00024Companion_getRrnUrl(JNIEnv *env,
                                                                                jobject thiz) {
    return (*env)->NewStringUTF(env, "https://getrrn.netpluspay.com");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_nibss_Keys_getTestKeyOne(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "5D25072F04832A2329D93E4F91BA23A2");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_nibss_Keys_getTestKeyTwo(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "86CBCDE3B0A22354853E04521686863D");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_nibss_Keys_getEpmsLiveKeyOne(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "E6891F73948F16C4D6E979D68534D0F4");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_nibss_Keys_getEpmsLiveKeyTwo(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "3D10EF707F98E3543E32B570E9E9AE86");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_nibss_Keys_getPosvasLiveKeyOne(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "9BF76D3E13ADD67A51549B7C3EB0E3AD");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_nibss_Keys_getPosvasLiveKeyTwo(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "A4BAEC5E31BFD913919262C7A7A76D52");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_service_SocketClient_getNibssIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.18");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_service_SocketClient_getNibssPort(JNIEnv *env, jclass clazz) {
    return (*env)->NewStringUTF(env, "5016");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.18");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "5016");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssTestUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "epms.test.netpluspay.com");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssTestPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "6868");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNetPlusPayUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://device.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getDefaultTid(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "2057H63U");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssTestIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.10");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssConnectionTestPort(JNIEnv *env,
                                                                              jobject thiz) {
    return (*env)->NewStringUTF(env, "55533");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssConnectionTestKeyOne(JNIEnv *env,
                                                                                jobject thiz) {
    return (*env)->NewStringUTF(env, "5D25072F04832A2329D93E4F91BA23A2");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssConnectionTestKeyTwo(JNIEnv *env,
                                                                                jobject thiz) {
    return (*env)->NewStringUTF(env, "86CBCDE3B0A22354853E04521686863D");
}

JNIEXPORT jstring JNICALL
Java_com_netpluspay_nibssclient_util_UtilityParams_getNibssConnectionTestTid(JNIEnv *env,
                                                                             jobject thiz) {
    return (*env)->NewStringUTF(env, "20398A4C");
}