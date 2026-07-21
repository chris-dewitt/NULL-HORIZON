package com.nullhorizon.app

import android.app.Application
import com.nullhorizon.app.di.AppContainer
import com.nullhorizon.app.simulation.sql.AndroidSqlDatabaseFactory
import com.nullhorizon.app.simulation.sql.SqlEngine

class NullHorizonApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Mission SQL must use Android's SQLite; sqlite-jdbc has no native
        // library on Android. Register before any mission opens a database.
        SqlEngine.factory = AndroidSqlDatabaseFactory()
        container = AppContainer(this)
    }
}
