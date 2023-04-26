package it.alessandrof.carddetector.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import it.alessandrof.carddetector.databinding.ActivityMainBinding
import it.alessandrof.carddetector.ui.adapter.RecognitionAdapter
import it.alessandrof.carddetector.ui.viewmodel.RecognitionListViewModel
import it.alessandrof.carddetector.util.REQUEST_CODE_PERMISSIONS
import it.alessandrof.carddetector.util.REQUIRED_PERMISSIONS
import it.alessandrof.carddetector.util.TAG
import it.alessandrof.carddetector.R
import it.alessandrof.carddetector.ml.CardModel
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel
import it.alessandrof.carddetector.util.MAX_RESULT_DISPLAY
import it.alessandrof.carddetector.util.YuvToRgbConverter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var baseActivityBinding: ActivityMainBinding? = null
    private lateinit var binding: ActivityMainBinding

    // CameraX variables
    private lateinit var preview: Preview // Preview use case, fast, responsive view of the camera
    private lateinit var imageAnalyzer: ImageAnalysis // Analysis use case, for running ML code
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Views attachment
    private val resultRecyclerView by lazy {
        binding.recognitionResults // Display the result of analysis
    }
    private val viewFinder by lazy {
        binding.viewFinder // Display the preview image from Camera
    }

    // Contiene i Recognition risultati dall'analisi ad ogni frame. È un viewModel, quindi si occupa di gestire e mantenere i dati dell'activity per l'UI
    private val recogViewModel: RecognitionListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        baseActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        binding = baseActivityBinding as ActivityMainBinding

        setContentView(binding.root)

        // Permessi per la camera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Inizializzo resultRecyclerView e il suo viewAdaptor collegato
        val viewAdapter = RecognitionAdapter(this)
        resultRecyclerView.adapter = viewAdapter

        // Disabilito l'animator della view per evitare il flickering
        resultRecyclerView.itemAnimator = null

        // Creo un observer dei LiveData della recognitionList
        // Questo notificherà il recycler view di aggiornare ogni volta che una nuova lista di LiveData è disponibile
        recogViewModel.recognitionList.observe(this,
                                               Observer {
                                                   viewAdapter.submitList(it)
                                               }
        )

    }

    /**
     * Fa il check di tutti i permessi
     */
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * chiamato dopo che i permessi della Camera sono stati dati
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                startCamera()
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

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
                        analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(this) {  items  ->
                            items as List<RecognitionModel>

                            if(items[0].toStringProb()>40) {
                                recogViewModel.updateData(items)
                                Thread.sleep(3000)
                            }else{
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
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private class ImageAnalyzer(ctx: Context, private val listener: (Any) -> Unit) : ImageAnalysis.Analyzer {


        // creazione della variabile contenente il modello Tensorflow Lite da utilizzare nell'Analyzer
        private val cardModel = CardModel.newInstance(ctx)

        //image proxy corrisponde al frame i-esimo ricevuto dalla fotocamera posteriore
        override fun analyze(imageProxy: ImageProxy) {

            val items = mutableListOf<RecognitionModel>()

            // Converto l'immagine del frame in Bitmap e poi in TenorImage
            val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy))

            // Processo l'immagine usando un il modello e prendo i primi risultati che li salvo nella variabile
            val outputs : List<Category> = cardModel.process(tfImage)
                    .probabilityAsCategoryList.apply {
                        sortByDescending { it.score }
                    }.take(MAX_RESULT_DISPLAY)

            // Controllo che livello di confidenza ha il miglior risultato dato dal modello, poi lo converto in Recognition, ovvero un tipo ocmposto dalla label della carta e dal livello di confidenza
            if(outputs.get(0).score>(0.36).toFloat()) {
                if(outputs.get(0).score>(0.55).toFloat()) {
                    items.add(RecognitionModel(outputs.get(0).label, outputs.get(0).score))
                    Log.d(TAG, "SICURA "+outputs.get(0).score)
                }else{
                    items.add(RecognitionModel("Credo "+outputs.get(0).label, outputs.get(0).score))
                    Log.d(TAG, "CREDO "+outputs.get(0).score)
                }
            }else {
                items.add(RecognitionModel("Non riesco a riconoscere", outputs.get(0).score))
                Log.d(TAG, "NO "+outputs.get(0).label+" , "+outputs.get(0).score)
            }

            // ritorno al listener il risultato
            listener.apply {
                items.toList()
            }

            // Close the image,this tells CameraX to feed the next image to the analyzer
            imageProxy.close()
        }



        /**
         * Conversione dell'ImageProxy (il frame) in Bitmap, per poter essere poi letto dal modello Tensorflow Lite
         */
        private val yuvToRgbConverter = YuvToRgbConverter(ctx)
        private lateinit var bitmapBuffer: Bitmap
        private lateinit var rotationMatrix: Matrix

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

            val image = imageProxy.image ?: return null

            // Initialise Buffer
            if (!::bitmapBuffer.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()")
                rotationMatrix = Matrix()
                rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                bitmapBuffer = Bitmap.createBitmap(
                        imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                )
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                    bitmapBuffer,
                    0,
                    0,
                    bitmapBuffer.width,
                    bitmapBuffer.height,
                    rotationMatrix,
                    false
            )
        }

    }

}