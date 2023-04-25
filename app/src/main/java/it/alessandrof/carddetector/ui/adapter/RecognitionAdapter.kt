package it.alessandrof.carddetector.ui.adapter

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import it.alessandrof.carddetector.databinding.RecognitionItemBinding
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel
import java.util.*

class RecognitionAdapter(private val ctx: Context) :
    ListAdapter<RecognitionModel, RecognitionViewHolder>(RecognitionDiffUtil()) {

    /**
     * Lega il ViewHolder con il recognition_item layout e fa data binding
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecognitionViewHolder {
        val inflater = LayoutInflater.from(ctx)
        val binding = RecognitionItemBinding.inflate(inflater, parent, false)
        return RecognitionViewHolder(binding,ctx)
    }

    // Lega i campi dei dati al RecognitionViewHolder
    override fun onBindViewHolder(holder: RecognitionViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    //Aggiorno solo quando ho elementi nuovi
    private class RecognitionDiffUtil : DiffUtil.ItemCallback<RecognitionModel>() {
        override fun areItemsTheSame(oldItem: RecognitionModel, newItem: RecognitionModel): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: RecognitionModel, newItem: RecognitionModel): Boolean {
            return oldItem.confidence == newItem.confidence
        }
    }


}

class RecognitionViewHolder(private val binding: RecognitionItemBinding, private val ctx: Context) :
    RecyclerView.ViewHolder(binding.root) {

    //Text To Speech
    lateinit var mTTS: TextToSpeech

    // Lega i campi ai corrispettivi elementi della view
    fun bindTo(recognition: RecognitionModel) {

        binding.recognitionItem = recognition

        Log.d("Card Detector","Confidenza "+recognition.confidence)

        if(recognition.label!="")
            mTTS = TextToSpeech(ctx,TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR){
                    mTTS.language= Locale.ITALIAN
                    mTTS.speak(recognition.label, TextToSpeech.QUEUE_FLUSH, null)
                }else{
                    Log.d("Card Detector","Errore Text To Speech")
                }
            })


        binding.executePendingBindings()
    }
}