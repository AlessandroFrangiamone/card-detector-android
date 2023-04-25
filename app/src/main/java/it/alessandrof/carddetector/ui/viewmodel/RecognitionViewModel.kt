package it.alessandrof.carddetector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.alessandrof.carddetector.ui.adapter.items.recognition.RecognitionModel

class RecognitionViewModel: ViewModel() {

    // È un campo LiveData
    private val _recognitionList = MutableLiveData<List<RecognitionModel>>()
    val recognitionList: LiveData<List<RecognitionModel>> = _recognitionList

    fun updateData(recognitions: List<RecognitionModel>){
        _recognitionList.postValue(recognitions)
    }

}