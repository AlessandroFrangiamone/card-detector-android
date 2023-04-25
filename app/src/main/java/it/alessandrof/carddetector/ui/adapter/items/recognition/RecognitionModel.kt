package it.alessandrof.carddetector.ui.adapter.items.recognition

/**
 * Semplice oggetto con 2 campi: nome della label e probabilità
 */
data class RecognitionModel(
        val label:String,
        val confidence:Float
) {

    override fun toString():String{
        return "$label / $probabilityString"
    }

    fun toStringProb():Int{
        return (confidence*100).toInt()
    }

    // La probabilità è più semplice da trattare nella view come una stringa e non come un float (easy data binding)
    private val probabilityString = String.format("%.1f%%", confidence * 100.0f)

}