package com.android.rurutekcamview

import android.os.Parcel
import android.os.Parcelable

data class CameraInfo(
    var cameraName: String?,
    var ipAddress: String?,
    var streamingPath: String?,
    var rtspUsername: String?,
    var rtspPassword: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cameraName)
        parcel.writeString(ipAddress)
        parcel.writeString(streamingPath)
        parcel.writeString(rtspUsername)
        parcel.writeString(rtspPassword)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraInfo> {
        override fun createFromParcel(parcel: Parcel): CameraInfo {
            return CameraInfo(parcel)
        }

        override fun newArray(size: Int): Array<CameraInfo?> {
            return arrayOfNulls(size)
        }
    }
}
