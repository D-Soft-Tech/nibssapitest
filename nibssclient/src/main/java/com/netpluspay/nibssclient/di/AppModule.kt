package com.netpluspay.nibssclient.di

import android.content.Context
import com.netpluspay.nibssclient.dao.TransactionResponseDao
import com.netpluspay.nibssclient.database.AppDatabase
import com.netpluspay.nibssclient.network.StormApiService
import com.netpluspay.nibssclient.util.Constants.BASE_URL_FOR_LOGGING_TO_BACKEND
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesOKHTTPClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val cacheSize = (5 * 1024 * 1024).toLong()
        val mCache = Cache(context.cacheDir, cacheSize)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient().newBuilder()
            .cache(mCache)
            .connectTimeout(200, TimeUnit.SECONDS)
            .readTimeout(200, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .addInterceptor(loggingInterceptor)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun providesBaseUrl() = BASE_URL_FOR_LOGGING_TO_BACKEND

    @Singleton
    @Provides
    fun providesRetrofit(okhttp: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .client(okhttp)
            .build()

    @Singleton
    @Provides
    fun providesStormApiService(retrofit: Retrofit): StormApiService =
        retrofit.create(StormApiService::class.java)

    @Singleton
    @Provides
    fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabaseInstance(context)
    }

    @Singleton
    @Provides
    fun providesAppDao(appDatabase: AppDatabase): TransactionResponseDao {
        return appDatabase.transactionResponseDao()
    }
}
