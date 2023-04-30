package it.alessandrof.carddetector.ui.fragment

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import it.alessandrof.carddetector.databinding.FragmentImageDetectionBinding
import it.alessandrof.carddetector.ui.adapter.RecognitionAdapter
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel
import it.alessandrof.carddetector.ui.viewmodel.DetectionActivityViewModel
import it.alessandrof.carddetector.ui.viewmodel.RecognitionListViewModel
import it.alessandrof.carddetector.util.ImageAnalyzer
import it.alessandrof.carddetector.util.TAG
import java.util.concurrent.Executors

class ImageDetectionFragment: Fragment() {

    private lateinit var binding: FragmentImageDetectionBinding

    // Contiene i Recognition risultati dall'analisi ad ogni frame. È un viewModel, quindi si occupa di gestire e mantenere i dati dell'activity per l'UI
    private val recogViewModel: RecognitionListViewModel by viewModels()
    private val activityViewModel: DetectionActivityViewModel by viewModels()

    private lateinit var viewAdapter: RecognitionAdapter

    // CameraX variables
    private lateinit var preview: Preview // Preview use case, fast, responsive view of the camera
    private lateinit var imageAnalyzer: ImageAnalysis // Analysis use case, for running ML code
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private var cameraAlreadyStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageDetectionBinding.inflate(layoutInflater)

        viewAdapter = RecognitionAdapter(requireActivity())

        binding.recognitionResults.adapter = viewAdapter
        // Disabilito l'animator della view per evitare il flickering
        binding.recognitionResults.itemAnimator = null

        setUpObservers()
        startCamera()

        return binding.root
    }

    private fun setUpObservers(){
        activityViewModel.startCamera.observe(viewLifecycleOwner){
            if(it == true && !cameraAlreadyStarted) {
                cameraAlreadyStarted = true
                startCamera()
            }
        }

        recogViewModel.recognitionList.observe(this){
            viewAdapter.submitList(it)
        }
    }

    /**
     * I procedimenti da eseguire nel momento in cui la camera viene avviata sono
     *
     * 1. Inizializzare le preview
     * 2. Inizializzare l'image analyzer
     * 3. Proiettare entrambi nel lifecycle dell'activity
     * 4. Inviare i frame alla preview
     */
    private fun startCamera() {

        //Singleton che serve a legare la camera al processo dell'applicazione e permettere di fare l'analisi a queste immagini
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                    .build()

            imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysisUseCase: ImageAnalysis ->
                        analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(requireActivity()) { items ->
                            items as List<RecognitionModel>

                            if (items[0].toStringProb() > 40) {
                                recogViewModel.updateData(items)
                                Thread.sleep(3000)
                            } else {
                                Thread.sleep(1000)
                            }

                        })
                    }

            // Selezione della camera posteriore
            val cameraSelector =
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                        CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - try to bind everything at once and CameraX will find
                // the best combination.
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalyzer
                )

                // Legare la preview al View Finder, ovvero la View che si occupa di visualizzare la preview
                preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

}