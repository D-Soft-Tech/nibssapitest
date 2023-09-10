#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_isw_gateway_TransactionProcessorWrapper_getIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "30fcbc7d33e33b7432a92293836b6311");
}

JNIEXPORT jstring JNICALL
Java_com_isw_gateway_TransactionProcessorWrapper_getPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "4e9513a5064ffb9215486481c86d7d53");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_Utility_getDefaultIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "8a5ffbe6fc258bc36b57c0f3ca25df21");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_Utility_getDefaultPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "5cdf4ebe182ddef57d60310b5109f8e8");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "799f74f8efabba44083792c82634b747");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_di_AppModule_getSeK(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "=[ei}%#@%^+v[&=_");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "58cf3d845ff70214ada110c183d26a39");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssTestUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "8193c97750668b31713a8a0c286f4a264f903b30400e722f543a4985a9b561bf");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssTestPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "e6a51921752e6c0341f9ce33cc760792");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNetPlusPayUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "c3484f8fd42b603324ffd43dd83871e4ffc0ab103f9651da59f6574368e7f11a");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getDefaultTid(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "370d8a8aca4c80a524a2b810b65471f6");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssTestIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "30fcbc7d33e33b7432a92293836b6311");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestPort(JNIEnv *env,
                                                                            jobject thiz) {
    return (*env)->NewStringUTF(env, "4e9513a5064ffb9215486481c86d7d53");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestKeyOne(JNIEnv *env,
                                                                              jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "86d1c18b238183888c85fadc58bec1b84a50a460c8a9de4ad4155d43d8f7297fc6a0069dd0e3fcda22b44216668e24a3");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestKeyTwo(JNIEnv *env,
                                                                              jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "af07e4c205feababd7661ab92b4e9ccff089bfff7b61cdf4ec5214f3317a6e2704ca083729c2733543b9edc386cc3a36");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getNibssConnectionTestTid(JNIEnv *env,
                                                                           jobject thiz) {
    return (*env)->NewStringUTF(env, "cb39ab62567c33124743f9a348b76c92");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_di_AppModule_getSeiv(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "+p&*,#@!ki_7_+$#");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getSessionKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "54bc6b1e7b2a57871db851e94d4f7ea96924623877f93b9983752e6193f227d5");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getIpekTest(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "d01477ca17adc2bcb3b16674fc06ab911e35e80f34d5ec1b225654385087fe5f");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getKsnTest(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "e2886c9bde60e05e1ebf51ae3fa0c5d54139e253b7bbbd72d3894173e4d00dde");
}

JNIEXPORT jstring JNICALL
Java_com_danbamitale_epmslib_utils_UtilityParams_getKsnLive(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env,
                                "ccf6f772ee2cd3adadfb8a86f3e03a56c14af03ffa60accefe3bcf4b3581d131");
}