package com.netpluspay.nibssclient.network

import com.netpluspay.nibssclient.models.* // ktlint-disable no-wildcard-imports
import io.reactivex.Single
import retrofit2.http.* // ktlint-disable no-wildcard-imports

interface StormApiService {
    @POST("/pos_transaction")
    fun logTransactionBeforeMakingPayment(
        @Body dataToLog: TransactionToLogBeforeConnectingToNibbs
    ): Single<ResponseBodyAfterLoginToBackend>

    @PUT("/pos_transaction/{rrn}")
    fun updateLogAfterConnectingToNibss(
        @Path("rrn") rrn: String,
        @Body data: DataToLogAfterConnectingToNibss
    ): Single<LogToBackendResponse>

    @GET("/partners/{partnerId}/isw_threshold")
    fun getPartnerInterSwitchThreshold(
        @Path("partnerId") partnerId: String
    ): Single<GetPartnerInterSwitchThresholdResponse?>
}
