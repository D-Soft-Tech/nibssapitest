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

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getSeK(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "=[ei}%#@%^+v[&=_");
}


JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getSeiv(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "+p&*,#@!ki_7_+$#");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.18");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "4016");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssTestUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "epms.test.netpluspay.com");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssTestPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "6868");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNetPlusPayUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://device.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getDefaultTid(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "2057H63U");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssTestIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.10");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestPort(JNIEnv *env,
                                                                            jobject thiz) {
    return (*env)->NewStringUTF(env, "55533");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestKeyOne(JNIEnv *env,
                                                                              jobject thiz) {
    return (*env)->NewStringUTF(env, "5D25072F04832A2329D93E4F91BA23A2");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestKeyTwo(JNIEnv *env,
                                                                              jobject thiz) {
    return (*env)->NewStringUTF(env, "86CBCDE3B0A22354853E04521686863D");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestTid(JNIEnv *env,
                                                                           jobject thiz) {
    return (*env)->NewStringUTF(env, "20398A4C");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getSessionKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "3F2216D8297BCE9C");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getIpekTest(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "9F8011E7E71E483B");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getKsnTest(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "0000000006DDDDE01500");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getKsnLive(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "0000000002DDDDE00001");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_di_AppModule_getSeK(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "=[ei}%#@%^+v[&=_");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_di_AppModule_getSeiv(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "+p&*,#@!ki_7_+$#");
}