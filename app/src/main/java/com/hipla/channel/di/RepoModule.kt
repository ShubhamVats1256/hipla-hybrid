package com.hipla.channel.di

import com.hipla.channel.api_service.HiplaApiService
import com.hipla.channel.repo.HiplaRepo
import org.koin.dsl.module
import retrofit2.Retrofit

val repoModule = module {

    single {
        get<Retrofit>().create(HiplaApiService::class.java)
    }

    single {
        HiplaRepo(get())
    }

}