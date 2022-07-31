package com.hipla.channel.viewmodel

import com.hipla.channel.api.ifSuccessful
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class MainViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)

    fun loadUsers() {
        launchIO {
             val test = hiplaRepo.fetchTest().ifSuccessful {
                Timber.tag("testfx").d(it.fact)
            }
        }
    }


}