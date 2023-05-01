package it.alessandrof.carddetector.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.fragment.app.Fragment
import it.alessandrof.carddetector.R
import it.alessandrof.carddetector.ml.CardModel
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category

class ImageAnalyzer(ctx: Context, private val fragment: Fragment, private val listener: RecognitionListener) : ImageAnalysis.Analyzer {


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
        if(outputs[0].score>(0.36).toFloat()) {
            if(outputs[0].score>(0.55).toFloat()) {
                items.add(
                        RecognitionModel(
                                getCorrectString(
                                        fragment = fragment,
                                        label = outputs[0].label),
                                outputs[0].score
                        )
                )
                Log.d(TAG, "SICURA "+ outputs[0].score)
            }else{
                items.add(
                        RecognitionModel(
                                fragment.getString(R.string.probably) + getCorrectString(
                                        fragment = fragment,
                                        label = outputs[0].label
                                ),
                                outputs[0].score
                        )
                )
                Log.d(TAG, "PROBABILMENTE "+ outputs[0].score)
            }
        }else{
            items.add(
                    RecognitionModel(
                            fragment.getString(R.string.cant_recognize),
                            outputs[0].score
            ))
            Log.d(TAG, "NO "+ outputs[0].label+" , "+ outputs[0].score)
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
            Log.d(TAG, "Initialise toBitmap()")
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

    private fun getCorrectString(fragment: Fragment, label: String): String{
        return when(label){
            "jolly" -> fragment.getString(R.string.jolly)

            "asso di cuori" -> fragment.getString(R.string.ace_of_hearts)
            "asso di fiori" -> fragment.getString(R.string.ace_of_clubs)
            "asso di picche" -> fragment.getString(R.string.ace_of_spades)
            "asso di quadri" -> fragment.getString(R.string.ace_of_diamonds)

            "due di cuori" -> fragment.getString(R.string.two_of_hearts)
            "due di fiori" -> fragment.getString(R.string.two_of_clubs)
            "due di picche" -> fragment.getString(R.string.two_of_spades)
            "due di quadri" -> fragment.getString(R.string.two_of_diamonds)

            "tre di cuori" -> fragment.getString(R.string.three_of_hearts)
            "tre di fiori" -> fragment.getString(R.string.three_of_clubs)
            "tre di picche" -> fragment.getString(R.string.three_of_spades)
            "tre di quadri" -> fragment.getString(R.string.three_of_diamonds)

            "quattro di cuori" -> fragment.getString(R.string.four_of_hearts)
            "quattro di fiori" -> fragment.getString(R.string.four_of_clubs)
            "quattro di picche" -> fragment.getString(R.string.four_of_spades)
            "quattro di quadri" -> fragment.getString(R.string.four_of_diamonds)

            "cinque di cuori" -> fragment.getString(R.string.five_of_hearts)
            "cinque di fiori" -> fragment.getString(R.string.five_of_clubs)
            "cinque di picche" -> fragment.getString(R.string.five_of_spades)
            "cinque di quadri" -> fragment.getString(R.string.five_of_diamonds)

            "sei di cuori" -> fragment.getString(R.string.six_of_hearts)
            "sei di fiori" -> fragment.getString(R.string.six_of_clubs)
            "sei di picche" -> fragment.getString(R.string.six_of_spades)
            "sei di quadri" -> fragment.getString(R.string.six_of_diamonds)

            "sette di cuori" -> fragment.getString(R.string.seven_of_hearts)
            "sette di fiori" -> fragment.getString(R.string.seven_of_clubs)
            "sette di picche" -> fragment.getString(R.string.seven_of_spades)
            "sette di quadri" -> fragment.getString(R.string.seven_of_diamonds)

            "otto di cuori" -> fragment.getString(R.string.eight_of_hearts)
            "otto di fiori" -> fragment.getString(R.string.eight_of_clubs)
            "otto di picche" -> fragment.getString(R.string.eight_of_spades)
            "otto di quadri" -> fragment.getString(R.string.eight_of_diamonds)

            "nove di cuori" -> fragment.getString(R.string.nine_of_hearts)
            "nove di fiori" -> fragment.getString(R.string.nine_of_clubs)
            "nove di picche" -> fragment.getString(R.string.nine_of_spades)
            "nove di quadri" -> fragment.getString(R.string.nine_of_diamonds)

            "dieci di cuori" -> fragment.getString(R.string.ten_of_hearts)
            "dieci di fiori" -> fragment.getString(R.string.ten_of_clubs)
            "dieci di picche" -> fragment.getString(R.string.ten_of_spades)
            "dieci di quadri" -> fragment.getString(R.string.ten_of_diamonds)

            "jack di cuori" -> fragment.getString(R.string.jack_of_hearts)
            "jack di fiori" -> fragment.getString(R.string.jack_of_clubs)
            "jack di picche" -> fragment.getString(R.string.jack_of_spades)
            "jack di quadri" -> fragment.getString(R.string.jack_of_diamonds)

            "donna di cuori" -> fragment.getString(R.string.queen_of_hearts)
            "donna di fiori" -> fragment.getString(R.string.queen_of_clubs)
            "donna di picche" -> fragment.getString(R.string.queen_of_spades)
            "donna di quadri" -> fragment.getString(R.string.queen_of_diamonds)

            "re di cuori" -> fragment.getString(R.string.king_of_hearts)
            "re di fiori" -> fragment.getString(R.string.king_of_clubs)
            "re di picche" -> fragment.getString(R.string.king_of_spades)
            "re di quadri" -> fragment.getString(R.string.king_of_diamonds)

            else -> {
                "-"
            }
        }
    }

}
