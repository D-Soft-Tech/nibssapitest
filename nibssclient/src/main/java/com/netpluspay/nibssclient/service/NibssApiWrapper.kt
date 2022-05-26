package com.netpluspay.nibssclient.service

import android.content.Context
import com.danbamitale.epmslib.entities.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.isw.gateway.TransactionProcessorWrapper
import com.netpluspay.nibssclient.exception.createNibssException
import com.netpluspay.nibssclient.models.*
import com.netpluspay.nibssclient.util.SharedPrefManager
import com.netpluspay.nibssclient.util.Utility
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

object NibssApiWrapper {
    val gson = Gson()
    private val TAG = NibssApiWrapper::class.java.simpleName
    private var iswPaymentProcessorObject: TransactionProcessorWrapper? = null
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val user: User = User()

    @JvmStatic
    fun logUser(inputUserData: String) {
        val userData = gson.fromJson(inputUserData, UserData::class.java)
        SharedPrefManager.setUserData(userData)
    }

//    @JvmStatic
//    fun configureTerminal(context: Context, params: ConfigurationParams): Single<KeyHolder> =
//        Single.fromCallable {
//            if (params.validate().not())
//                throw "{\"code\":500,\"error\":\"Terminal Id and/or terminalSerial absent\"}".createNibssException(
//                    params.action
//                )
//            val req = gson.toJson(params)
//            val response = NibssApiClient.write(context, req)
//            val keyHolder = gson.fromJson(response, KeyHolder::class.java)
//            if (keyHolder == null || keyHolder.isValid.not())
//                throw response.createNibssException(params.action)
//            Singletons.settKeyHolder(keyHolder)
//            keyHolder
//        }

    @JvmStatic
    fun callHome(context: Context, params: ConfigurationParams): Single<String> =
        Single.fromCallable {
            params.action = "callHome"
            if (params.validate().not())
                throw "{\"code\":500,\"error\":\"Terminal Id and/or terminalSerial absent\"}".createNibssException(
                    params.action
                )
            val response = NibssApiClient.write(context, gson.toJson(params))
            if (response != "00")
                throw response.createNibssException(params.action)
            response
        }

    @JvmStatic
    fun makePayment(context: Context, params: MakePaymentParams): Single<TransactionResponse> =
        Single.fromCallable {
            if (params.amount < 200)
                throw "{\"code\":500,\"error\":\"Amount should not be less than 200 kobo\"}".createNibssException(
                    params.action
                )
            if (params.validate().not())
                throw "{\"code\":500,\"error\":\"Error, check your inputs\"}".createNibssException(
                    params.action
                )
            val req = gson.toJson(params)
            val response = NibssApiClient.write(context, req)
            val transactionResponse = gson.fromJson(response, TransactionResponse::class.java)
            if (transactionResponse == null || transactionResponse.validateResponse().not())
                throw response.createNibssException(params.action)
            transactionResponse
        }

    @JvmStatic
    fun checkBalance(context: Context, params: CheckBalanceParams): Single<TransactionResponse> =
        Single.fromCallable {
            val response = NibssApiClient.write(context, gson.toJson(params))
            var transactionResponse = gson.fromJson(response, TransactionResponse::class.java)
            if (transactionResponse == null || transactionResponse.validateResponse().not())
                transactionResponse = Utility.gatewayErrorTransactionResponse(
                    0,
                    TransactionType.BALANCE,
                    params.accountType,
                    errorMessage = response
                )
            transactionResponse
        }

    @JvmStatic
    fun refundTransaction(
        context: Context,
        params: RefundTransactionParams
    ): Single<TransactionResponse> = Single.fromCallable {
        params.originalDataElements = params.transactionResponse!!.toOriginalDataElements()
        params.transactionResponse = null
        val response = NibssApiClient.write(context, gson.toJson(params))
        var transactionResponse = gson.fromJson(response, TransactionResponse::class.java)
        if (transactionResponse == null || transactionResponse.validateResponse().not())
            transactionResponse = Utility.gatewayErrorTransactionResponse(
                params.originalDataElements!!.originalAmount,
                TransactionType.REVERSAL,
                params.accountType,
                errorMessage = response
            )
        transactionResponse
    }

    @JvmStatic
    fun downloadNibssAIDS(context: Context, params: ConfigurationParams): Single<List<NibssAID>> =
        Single.fromCallable {
            params.action = "downloadNibssAID"
            val response = NibssApiClient.write(context, gson.toJson(params))
            val myType = object : TypeToken<List<NibssAID>>() {}.type
            val aidList = gson.fromJson<List<NibssAID>>(response, myType)
            if (aidList.isNullOrEmpty())
                throw response.createNibssException(params.action)
            aidList
        }

    @JvmStatic
    fun downloadNibssCAPKS(context: Context, params: ConfigurationParams): Single<List<NibssCA>> =
        Single.fromCallable {
            params.action = "downloadNibssCAPK"
            val response = NibssApiClient.write(context, gson.toJson(params))
            val myType = object : TypeToken<List<NibssCA>>() {}.type
            val capkList = gson.fromJson<List<NibssCA>>(response, myType)
            if (capkList.isNullOrEmpty())
                throw response.createNibssException(params.action)
            capkList
        }

    @JvmStatic
    fun completion(context: Context, params: MakePaymentParams): Single<TransactionResponse> =
        Single.fromCallable {
            params.action = "completion"
            val response = NibssApiClient.write(context, gson.toJson(params))
            var transactionResponse = gson.fromJson(response, TransactionResponse::class.java)
            if (transactionResponse == null || transactionResponse.validateResponse().not())
                transactionResponse = Utility.gatewayErrorTransactionResponse(
                    params.originalDataElements!!.originalAmount,
                    params.transactionType,
                    params.accountType
                )
            transactionResponse
        }
}
