package it.alessandrof.carddetector.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import it.alessandrof.carddetector.ml.CardModel
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category

class ImageAnalyzer(ctx: Context, private val listener: RecognitionListener) : ImageAnalysis.Analyzer {


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

        listener.invoke(
                items.toList()
        )

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
