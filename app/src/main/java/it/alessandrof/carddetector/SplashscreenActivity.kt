package it.alessandrof.carddetector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import it.alessandrof.carddetector.databinding.ActivitySplashScreenBinding
import it.alessandrof.carddetector.ui.activity.MainActivity

class SplashscreenActivity : AppCompatActivity() {

    private var baseActivityBinding: ActivitySplashScreenBinding? = null
    private lateinit var binding: ActivitySplashScreenBinding

    private val SPLASH_SCREEN = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        baseActivityBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        binding = baseActivityBinding as ActivitySplashScreenBinding

        startTimer()

        setContentView(binding.root)

    }

    fun startTimer(){
        val r = Runnable {
            val intent = Intent(this, MainActivity::class.java).apply {
                Log.d("TAG", "Change Activity")
            }
            startActivity(intent)
        }
        Handler().postDelayed(r, (SPLASH_SCREEN).toLong())
    }
}