package com.estef.antiphishingcoach.app

import android.app.Application

/**
 * Punto de entrada global de la app.
 * En el MVP A se mantiene ligera y sin inicializaciones invasivas.
 */
class AntiPhishingCoachApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
