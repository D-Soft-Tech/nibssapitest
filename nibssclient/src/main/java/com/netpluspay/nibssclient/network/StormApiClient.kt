package com.netpluspay.nibssclient.network

import com.netpluspay.nibssclient.util.Constants.BASE_URL_FOR_LOGGING_TO_BACKEND
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class StormApiClient {

    companion object {

        private fun getBaseOkhttpClientBuilder(): OkHttpClient.Builder {
            val okHttpClientBuilder = OkHttpClient.Builder()

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)

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
    }
}
