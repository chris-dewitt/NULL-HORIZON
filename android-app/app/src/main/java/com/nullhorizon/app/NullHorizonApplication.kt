package com.nullhorizon.app

import android.app.Application
import com.nullhorizon.app.di.AppContainer

class NullHorizonApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
