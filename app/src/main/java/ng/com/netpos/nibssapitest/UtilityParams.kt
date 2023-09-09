package ng.com.netpos.nibssapitest

object UtilityParams {
    init {
        System.loadLibrary("api-keys")
    }

    private external fun getSeK2(): String
    private external fun getSeiv2(): String

    val STRING_REQ_CRED_SEC_K2 = getSeK2()
    val STRING_REQ_CRED_IV_2 = getSeiv2()
}
