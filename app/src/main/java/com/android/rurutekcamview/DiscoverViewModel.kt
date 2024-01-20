package com.android.rurutekcamview

import androidx.lifecycle.ViewModel

class DiscoverViewModel : ViewModel() {
    private var viewModelResponses: ArrayList<String> = ArrayList()

    var responses: ArrayList<String>
        get() = viewModelResponses
        set(value) {
            viewModelResponses = value
        }
}

