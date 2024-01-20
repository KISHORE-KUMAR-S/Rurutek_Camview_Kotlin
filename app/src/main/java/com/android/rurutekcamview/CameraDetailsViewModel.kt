package com.android.rurutekcamview

import androidx.lifecycle.ViewModel

class CameraDetailsViewModel : ViewModel() {
    private var _rtspUrl: String? = null

    var rtspUrl: String?
        get() = _rtspUrl
        set(value) {
            _rtspUrl = value
        }
}
