package com.hipla.channel.di

import com.hipla.channel.api.HiplaApiService
import com.hipla.channel.repo.HiplaRepo
import org.koin.core.context.GlobalContext
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
//
//val uiModule = module {
//
//    factory { FavAdapter() }
//
//    viewModel { SplashActivityViewModel(get()) }
//    viewModel { PhotosActivityViewModel(get(), GlobalContext.get().koin.get<PhotosRepository>()) }
//
//}