package com.hipla.channel.foodModule.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Networking {

    //private var retrofit: Retrofit? = null
    fun create(baseUrl: String,context: Context): NetworkService? {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)



        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor)
//        .addInterceptor(get<ChuckerInterceptor>())
            .addInterceptor(ChuckerInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(NetworkService::class.java)
    }
}