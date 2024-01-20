package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class EditActivity : AppCompatActivity() {
    private var etIpAddress: EditText? = null
    private var etStreamingPath: EditText? = null
    private var etCameraName: EditText? = null
    private var etRtspUsername: EditText? = null
    private var etRtspPassword: EditText? = null

    private var cameraInfo: CameraInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        setStatusBarTextColor()

        etIpAddress = findViewById(R.id.etIpAddress)
        etStreamingPath = findViewById(R.id.etStreamingPath)
        etCameraName = findViewById(R.id.etcamnam)
        etRtspUsername = findViewById(R.id.etRtspUsername)
        etRtspPassword = findViewById(R.id.etRtspPassword)
        val save = findViewById<Button>(R.id.bnSave)
        val back = findViewById<ImageButton>(R.id.back)

        cameraInfo = intent.getParcelableExtra("cameraInfo")

        assert(cameraInfo != null)
        etIpAddress?.setText(cameraInfo!!.ipAddress)
        etStreamingPath?.setText(cameraInfo!!.streamingPath)
        etCameraName?.setText(cameraInfo!!.cameraName)
        etRtspUsername?.setText(cameraInfo!!.rtspUsername)
        etRtspPassword?.setText(cameraInfo!!.rtspPassword)

        save.setOnClickListener { handleSaveChanges() }
        back.setOnClickListener { finish() }
    }

    private fun handleSaveChanges() {
        val editedRtspIpAddress = etIpAddress!!.text.toString().trim { it <= ' ' }
        val editedRtspStreamingPath = etStreamingPath!!.text.toString().trim { it <= ' ' }
        val editedRtspUsername = etRtspUsername!!.text.toString().trim { it <= ' ' }
        val editedRtspPassword = etRtspPassword!!.text.toString().trim { it <= ' ' }
        val editedCameraName = etCameraName!!.text.toString().trim { it <= ' ' }

        if (editedCameraName.isEmpty()) {
            Toast.makeText(this, "Camera name cannot be empty", Toast.LENGTH_SHORT).show()
        } else if (editedRtspUsername.isEmpty()) {
            Toast.makeText(this, "RTSP Username cannot be empty", Toast.LENGTH_SHORT).show()
        } else if (editedRtspPassword.isEmpty()) {
            Toast.makeText(this, "RTSP Password cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            cameraInfo!!.ipAddress = editedRtspIpAddress
            cameraInfo!!.streamingPath = editedRtspStreamingPath
            cameraInfo!!.cameraName = editedCameraName
            cameraInfo!!.rtspUsername = editedRtspUsername
            cameraInfo!!.rtspPassword = editedRtspPassword
            SaveCameraDetailsTask().execute(cameraInfo)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setStatusBarTextColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
            }
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    private inner class SaveCameraDetailsTask : AsyncTask<CameraInfo, Void, Int>(){
        override fun doInBackground(vararg params: CameraInfo?): Int {
            val ci = params[0]!!
            val dbHelper = CameraDetailsDbHelper(this@EditActivity)
            val db = dbHelper.writableDatabase

            return try {
                val whereClause = CameraDetailsDbHelper.IP_ADDRESS + " = ?"
                val whereArgs = arrayOf(ci.ipAddress)
                val values = ContentValues()
                values.put(CameraDetailsDbHelper.IP_ADDRESS, ci.ipAddress)
                values.put(CameraDetailsDbHelper.STREAMING_PATH, ci.streamingPath)
                values.put(CameraDetailsDbHelper.RTSP_USERNAME, ci.rtspUsername)
                values.put(CameraDetailsDbHelper.RTSP_PASSWORD, ci.rtspPassword)
                values.put(CameraDetailsDbHelper.CAMERA_NAME, ci.cameraName)

                // Update the camera data in the database
                db.update(CameraDetailsDbHelper.TABLE_NAME, values, whereClause, whereArgs)
            } catch (e: SQLiteException) {
                e.printStackTrace()
                -1
            } finally {
                dbHelper.close()
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(rowsUpdated: Int) {
            super.onPostExecute(rowsUpdated)
            if (rowsUpdated > 0) {
                Toast.makeText(this@EditActivity, "Camera Details updated", Toast.LENGTH_SHORT)
                    .show()
                val resultIntent = Intent()
                resultIntent.putExtra("updatedCameraInfo", cameraInfo)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(
                    this@EditActivity,
                    "Failed to update camera details. No matching record found.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}