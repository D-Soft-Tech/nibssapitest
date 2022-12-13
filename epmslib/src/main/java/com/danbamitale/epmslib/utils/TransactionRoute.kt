package com.danbamitale.epmslib.utils

enum class TransactionRoute(val routeName: String, val routeCode: Int) {
    NIBSS("nibss", 1),
    INTERSWITCH("interswitch", 2),
    ALL("ALL", 3)
}

enum class CVMETHOD {
    OFFLINE_PLAINTEXT_PIN,
    ONLINE_PIN,
    OFFLINE_PLAINTEXT_PIN_AND_SIGNATURE,
    OFFLINE_ENCIPHERED_PIN,
    OFFLINE_ENCIPHERED_PIN_AND_SIGNATURE,
    SIGNATURE,
    NO_CVM_PERFORMED
}
