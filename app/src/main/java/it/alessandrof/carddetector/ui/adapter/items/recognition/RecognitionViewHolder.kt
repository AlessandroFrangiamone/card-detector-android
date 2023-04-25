package it.alessandrof.carddetector.ui.adapter.items.recognition

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import it.alessandrof.carddetector.databinding.RecognitionItemBinding
import java.util.Locale

class RecognitionViewHolder(private val binding: RecognitionItemBinding, private val ctx: Context) :
    RecyclerView.ViewHolder(binding.root) {

    //Text To Speech
    lateinit var mTTS: TextToSpeech

    // Lega i campi ai corrispettivi elementi della view
    fun bindTo(recognition: RecognitionModel) {

        binding.recognitionItem = recognition

        Log.d("Card Detector", "Confidenza "+recognition.confidence)

        if(recognition.label!="")
            mTTS = TextToSpeech(ctx, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR){
                    mTTS.language= Locale.ITALIAN
                    mTTS.speak(recognition.label, TextToSpeech.QUEUE_FLUSH, null)
                }else{
                    Log.d("Card Detector", "Errore Text To Speech")
                }
            })


        binding.executePendingBindings()
    }
}