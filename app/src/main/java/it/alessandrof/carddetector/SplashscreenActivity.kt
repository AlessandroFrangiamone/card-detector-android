package it.alessandrof.carddetector

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import it.alessandrof.carddetector.databinding.ActivitySplashScreenBinding
import it.alessandrof.carddetector.ui.activity.HomepageActivity

@SuppressLint("CustomSplashScreen")
class SplashscreenActivity : AppCompatActivity() {

    private var baseActivityBinding: ActivitySplashScreenBinding? = null
    private lateinit var binding: ActivitySplashScreenBinding

    private val splashScreenTime = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        baseActivityBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        binding = baseActivityBinding as ActivitySplashScreenBinding

        startTimer()

        setContentView(binding.root)

    }

    private fun startTimer(){
        val r = Runnable {
            val intent = Intent(this, HomepageActivity::class.java).apply {
                Log.d("TAG", "Change Activity")
            }
            startActivity(intent)
        }
        Handler().postDelayed(r, (splashScreenTime).toLong())
    }
}