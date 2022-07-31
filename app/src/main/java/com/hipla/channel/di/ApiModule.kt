package com.hipla.channel.di

import com.hipla.channel.BuildConfig
import com.hipla.channel.common.adapter.JSONObjectAdapter
import com.hipla.channel.safeProceed
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val apiModule = module {

    // okhttp client builder
    single {
        val httpBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            httpBuilder.addNetworkInterceptor(HttpLoggingInterceptor().apply {
                HttpLoggingInterceptor.Level.BASIC
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
                MoshiConverterFactory.create()
            )
            .client(get())
            .baseUrl(BuildConfig.BASE_URL).build()
    }

}

