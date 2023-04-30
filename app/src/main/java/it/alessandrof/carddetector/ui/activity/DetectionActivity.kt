package it.alessandrof.carddetector.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import it.alessandrof.carddetector.databinding.ActivityDetectionBinding
import it.alessandrof.carddetector.util.REQUEST_CODE_PERMISSIONS
import it.alessandrof.carddetector.util.REQUIRED_PERMISSIONS
import it.alessandrof.carddetector.R
import it.alessandrof.carddetector.ui.viewmodel.DetectionActivityViewModel

class DetectionActivity : AppCompatActivity() {

    private var baseActivityBinding: ActivityDetectionBinding? = null
    private lateinit var binding: ActivityDetectionBinding

    private lateinit var navController: NavController

    private val activityViewModel: DetectionActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        baseActivityBinding = ActivityDetectionBinding.inflate(layoutInflater)
        binding = baseActivityBinding as ActivityDetectionBinding

        setContentView(binding.root)

        if (allPermissionsGranted()) {
            activityViewModel.setStartCamera(true)
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        setNavController()

        setUpListeners()
    }

    //Check di tutti i permessi
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    //chiamato dopo che i permessi della Camera sono stati dati
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                activityViewModel.setStartCamera(true)
            } else {
                // Esco dall'app
                Toast.makeText(
                        this,
                        getString(R.string.permission_deny_text),
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setNavController(){
        navController = findNavController(R.id.nav_host_fragment_main)
        navController.setGraph(
                R.navigation.nav_detection
        )
    }

    private fun setUpListeners(){
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

}