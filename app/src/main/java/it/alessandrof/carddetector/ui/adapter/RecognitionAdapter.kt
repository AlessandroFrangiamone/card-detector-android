package it.alessandrof.carddetector.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import it.alessandrof.carddetector.databinding.RecognitionItemBinding
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionViewHolder

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