package com.android.rurutekcamview

class CameraDetails {
    var ipAddress: String? = null
        private set

    var streamingPath: String? = null
        private set

    var cameraName: String? = null
        private set

    var rtspUsername: String? = null
        private set

    var rtspPassword: String? = null
        private set

    fun setIpAddress(ipAddress: String) {
        this.ipAddress = ipAddress
    }

    fun setStreamingPath(streamingPath: String) {
        this.streamingPath = streamingPath
    }

    fun setCameraName(cameraName: String) {
        this.cameraName = cameraName
    }

    fun setRtspUsername(rtspUsername: String) {
        this.rtspUsername = rtspUsername
    }

    fun setRtspPassword(rtspPassword: String) {
        this.rtspPassword = rtspPassword
    }
}
