package com.android.rurutekcamview

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase


class CameraDetailsDataSource(context: Context?) {
    private var database: SQLiteDatabase? = null
    private val dbHelper: CameraDetailsDbHelper

    init {
        dbHelper = context?.let { CameraDetailsDbHelper(it) }!!
    }

    fun open() {
        database = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    fun saveCameraDetails(cameraDetails: CameraDetails): Long {
        val values = ContentValues()
        values.put(CameraDetailsDbHelper.IP_ADDRESS, cameraDetails.ipAddress)
        values.put(CameraDetailsDbHelper.STREAMING_PATH, cameraDetails.streamingPath)
        values.put(CameraDetailsDbHelper.CAMERA_NAME, cameraDetails.cameraName)
        values.put(CameraDetailsDbHelper.RTSP_USERNAME, cameraDetails.rtspUsername)
        values.put(CameraDetailsDbHelper.RTSP_PASSWORD, cameraDetails.rtspPassword)
        return try {
            database!!.insertOrThrow(CameraDetailsDbHelper.TABLE_NAME, null, values)
        } catch (e: SQLiteConstraintException) {
            e.printStackTrace()
            -1
        }
    }

    fun isIpAddressExists(ipAddress: String): Boolean {
        val db = dbHelper.readableDatabase
        val selection = "ip_address=?"
        val selectionArgs = arrayOf(ipAddress)
        val cursor = db.query(
            CameraDetailsDbHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}

