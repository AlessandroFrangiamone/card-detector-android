package it.alessandrof.carddetector.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import it.alessandrof.carddetector.R
import it.alessandrof.carddetector.databinding.ActivityHomepageBinding

class HomepageActivity : AppCompatActivity() {

    private var baseActivityBinding: ActivityHomepageBinding? = null
    private lateinit var binding: ActivityHomepageBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        baseActivityBinding = ActivityHomepageBinding.inflate(layoutInflater)
        binding = baseActivityBinding as ActivityHomepageBinding

        setContentView(binding.root)

        setNavController()
    }

    private fun setNavController(){
        navController = findNavController(R.id.nav_host_homepage)
        navController.setGraph(
                R.navigation.nav_homepage
        )
    }

}