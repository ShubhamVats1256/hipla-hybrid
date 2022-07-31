package com.hipla.channel

import android.app.Application
import android.util.Log
import com.hipla.channel.di.apiModule
import com.hipla.channel.di.repoModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class HiplaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initApp()
    }

    private fun initApp() {
        setUpTimber()
        setUpKoin()
    }

    private fun setUpTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun setUpKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@HiplaApp)
            modules(
                listOf(
                    apiModule,
                    repoModule
                )
            )
        }
    }

}