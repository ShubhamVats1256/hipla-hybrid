package com.hipla.channel.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.hipla.channel.BuildConfig
import com.hipla.channel.common.adapter.JSONObjectAdapter
import com.hipla.channel.safeProceed
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

val apiModule = module {

    // okhttp client builder
    single {
        Timber.tag("hipla").d("adding logging interceptor 1")
        val httpBuilder = OkHttpClient.Builder()
        httpBuilder.readTimeout(20, TimeUnit.SECONDS);
        httpBuilder.connectTimeout(20, TimeUnit.SECONDS);
        httpBuilder.connectTimeout(20, TimeUnit.SECONDS) //Backend is really slow

        if (BuildConfig.DEBUG) {
            httpBuilder.addInterceptor(get<ChuckerInterceptor>())
            Timber.tag("hipla").d("adding logging interceptor2 ")
            httpBuilder.addNetworkInterceptor(HttpLoggingInterceptor().apply {
                HttpLoggingInterceptor.Level.BODY
            })

        }
        return@single httpBuilder
    }

    // okhttp client
    single {
        val okHttpBuilder = get<OkHttpClient.Builder>()
        okHttpBuilder.addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.header("Accept", "application/json")
            //requestBuilder.header("Authorization", BuildConfig.TOKEN)
            chain.safeProceed(requestBuilder.build())
        }.build()
    }

    // moshi
    single {
        Moshi.Builder()
            .add(JSONObjectAdapter)
            .build()
    }

    // retrofit instances
    single {
        Retrofit.Builder()
            .addConverterFactory(
                MoshiConverterFactory.create(get())
            )
            .client(get())
            .baseUrl(BuildConfig.BASE_URL).build()
    }

    single {
        ChuckerInterceptor.Builder(get())
            .collector(
                ChuckerCollector(
                    context = get(),
                    showNotification = true,
                    retentionPeriod = RetentionManager.Period.ONE_HOUR
                )
            )
            .maxContentLength(250_000L)
            .redactHeaders("Auth-Token", "Bearer")
            .alwaysReadResponseBody(true)
            .build()

    }


}

