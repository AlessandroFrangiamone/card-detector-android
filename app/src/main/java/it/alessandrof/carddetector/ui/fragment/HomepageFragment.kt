package it.alessandrof.carddetector.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.alessandrof.carddetector.databinding.FragmentHomepageBinding
import it.alessandrof.carddetector.ui.activity.DetectionActivity

class HomepageFragment: Fragment() {

    private lateinit var binding: FragmentHomepageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomepageBinding.inflate(layoutInflater)

        setUpListeners()

        return binding.root
    }

    private fun setUpListeners(){
        binding.startScan.setOnClickListener {
            val intent = Intent(activity, DetectionActivity::class.java)
            startActivity(intent)
        }
    }

}