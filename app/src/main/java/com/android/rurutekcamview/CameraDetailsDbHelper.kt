package com.android.rurutekcamview

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class CameraDetailsDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    val allCameraDetails: Cursor
        get() {
            val db = this.readableDatabase
            val orderBy = "$CAMERA_NAME ASC"
            return db.query(TABLE_NAME, null, null, null, null, null, orderBy)
        }

    companion object {
        private const val DATABASE_NAME = "CameraDetails.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "save_camera_details"
        private const val _ID = "_id"
        const val IP_ADDRESS = "ip_address"
        const val STREAMING_PATH = "streaming_path"
        const val CAMERA_NAME = "camera_name"
        const val RTSP_USERNAME = "rtsp_username"
        const val RTSP_PASSWORD = "rtsp_password"
        private const val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IP_ADDRESS + " TEXT," +
                STREAMING_PATH + " TEXT," +
                CAMERA_NAME + " TEXT UNIQUE, " +
                RTSP_USERNAME + " TEXT," +
                RTSP_PASSWORD + " TEXT)"
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}

