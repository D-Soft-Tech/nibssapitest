package com.netpluspay.nibssclient.util

import com.danbamitale.epmslib.entities.AccountBalance
import com.netpluspay.nibssclient.models.AccountBalanceResponse
import kotlin.reflect.full.memberProperties

object Mapper {
    fun AccountBalance.toAccountBalanceResponse(): AccountBalanceResponse =
        with(::AccountBalanceResponse) {
            val propertiesByName = AccountBalance::class.memberProperties.associateBy { it.name }
            callBy(
                parameters.associateWith { parameter ->
                    when (parameter.name) {
                        AccountBalanceResponse::accountType.name -> propertiesByName[amountType]?.name
                        else -> propertiesByName[parameter.name]?.get(this@toAccountBalanceResponse)
                    }
                }
            )
        }

    fun AccountBalance.mapToAccountBalanceResponse(): AccountBalanceResponse =
        AccountBalanceResponse(accountType.name, amountType, currencyCode, amountSign, amount)
}
