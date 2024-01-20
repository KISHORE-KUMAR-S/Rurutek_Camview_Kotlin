package com.android.rurutekcamview

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

class CameraDataLoader(context: Context) : AsyncTaskLoader<ArrayList<CameraInfo>>(context) {

    override fun onStartLoading() {
        forceLoad()
    }

    override fun loadInBackground(): ArrayList<CameraInfo> {
        // Implement the logic to load camera data here
        // For example, you can load data from a database or network
        return loadDataFromSource()
    }

    private fun loadDataFromSource(): ArrayList<CameraInfo> {
        // Replace this with your actual implementation to load camera data
        // Add your logic to populate cameraList
        return ArrayList()
    }
}