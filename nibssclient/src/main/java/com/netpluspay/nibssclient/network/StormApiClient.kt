package com.netpluspay.nibssclient.network

import com.netpluspay.nibssclient.util.Constants.BASE_URL_FOR_LOGGING_TO_BACKEND
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class StormApiClient {

    init {
        System.loadLibrary("api-keys")
    }

    companion object {

        private fun getBaseOkhttpClientBuilder(): OkHttpClient.Builder {
            val okHttpClientBuilder = OkHttpClient.Builder()

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)

            return okHttpClientBuilder
        }

        private fun getBaseOkhttpClientBuilderForRrn(): OkHttpClient.Builder {
            val okHttpClientBuilder = OkHttpClient.Builder()

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)

            return okHttpClientBuilder
        }

        private var LOGGING_INSTANCE: StormApiService? = null
        fun getStormApiLoginInstance(): StormApiService = LOGGING_INSTANCE ?: synchronized(this) {
            LOGGING_INSTANCE ?: Retrofit.Builder()
                .baseUrl(BASE_URL_FOR_LOGGING_TO_BACKEND)
                .client(getBaseOkhttpClientBuilder().build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StormApiService::class.java)
                .also {
                    LOGGING_INSTANCE = it
                }
        }

        private var RRN_LOGGING_INSTANCE: RrnApiService? = null
        fun getRrnServiceInstance(): RrnApiService = RRN_LOGGING_INSTANCE ?: synchronized(this) {
            RRN_LOGGING_INSTANCE ?: Retrofit.Builder()
                .baseUrl(getRrnUrl())
                .client(getBaseOkhttpClientBuilderForRrn().build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RrnApiService::class.java)
                .also {
                    RRN_LOGGING_INSTANCE = it
                }
        }

        private external fun getRrnUrl(): String
    }
}
