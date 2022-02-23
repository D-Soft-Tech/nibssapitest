package com.netpluspay.nibssclient.models


enum class IsoAccountType (val code: String) {
    DEFAULT_UNSPECIFIED("00"), SAVINGS("10"), CURRENT("20"), CREDIT("30"),
    UNIVERSAL_ACCOUNT("40"), INVESTMENT_ACCOUNT("50"), BONUS_ACCOUNT("91");



    override fun toString(): String {
        return name.replace('_', ' ').toLowerCase().capitalize()
    }

    companion object {
        fun parseStringAccountType(acctType: String) = when (acctType.toLowerCase()) {
            "savings", "saving" -> IsoAccountType.SAVINGS
            "current" -> IsoAccountType.CURRENT
            "credit" -> IsoAccountType.CREDIT
            "universal" -> IsoAccountType.UNIVERSAL_ACCOUNT
            "investment" -> IsoAccountType.INVESTMENT_ACCOUNT
            "bonus" -> IsoAccountType.BONUS_ACCOUNT
            else -> IsoAccountType.DEFAULT_UNSPECIFIED
        }

        fun parseIntAccountType(type: Int) =  when (type) {
            1, 10 -> IsoAccountType.SAVINGS

            2, 20 -> IsoAccountType.CURRENT

            3, 30 -> IsoAccountType.CREDIT

            4, 40 -> IsoAccountType.UNIVERSAL_ACCOUNT

            5, 50 -> IsoAccountType.INVESTMENT_ACCOUNT
            9, 91 -> IsoAccountType.BONUS_ACCOUNT

            else -> IsoAccountType.DEFAULT_UNSPECIFIED
        }
    }
}


