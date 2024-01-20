package com.android.rurutekcamview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


internal class CameraViewModel : ViewModel() {
    private val cameraInfo = MutableLiveData<CameraInfo>()

    fun setCameraInfo(info: CameraInfo) {
        cameraInfo.value = info
    }

    fun getCameraInfo(): MutableLiveData<CameraInfo> {
        return cameraInfo
    }
}