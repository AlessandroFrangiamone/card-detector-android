package it.alessandrof.carddetector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetectionActivityViewModel: ViewModel() {

    private val _startCamera = MutableLiveData<Boolean>(false)
    val startCamera: LiveData<Boolean> = _startCamera

    fun setStartCamera(enable: Boolean){
        _startCamera.postValue(enable)
    }

}