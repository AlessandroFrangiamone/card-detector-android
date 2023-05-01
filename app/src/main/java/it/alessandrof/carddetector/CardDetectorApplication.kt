package it.alessandrof.carddetector

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class CardDetectorApplication: Application() {

    @Override
    override fun onCreate() {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
    }

}