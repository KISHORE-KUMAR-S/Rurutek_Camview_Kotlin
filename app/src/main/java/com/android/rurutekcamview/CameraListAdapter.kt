package com.android.rurutekcamview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CameraListAdapter(
    private val context: Context,
    private val cameraInfoList: List<CameraInfo>,
    private val dataChangeListener: CameraDataChangeListener
) : BaseExpandableListAdapter() {

    private val editRequestCode = 1

    override fun getGroupCount(): Int {
        return cameraInfoList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return cameraInfoList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return cameraInfoList[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.list_item_group, null)
        }

        val cameraName = view!!.findViewById<TextView>(R.id.textCameraName)
        val ipAddress = view.findViewById<TextView>(R.id.textIpAddress)
        val streamingPort = view.findViewById<TextView>(R.id.textStreamingPort)

        val cameraInfo = getGroup(groupPosition) as CameraInfo

        cameraInfo.let {
            cameraName.text = it.cameraName
            ipAddress.text = it.ipAddress
            streamingPort.text = it.streamingPath
        }
        return view
    }

    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.list_item_child, null)
        }

        val playButton = view!!.findViewById<ImageButton>(R.id.play)
        val editButton = view.findViewById<ImageButton>(R.id.edit)
        val deleteButton = view.findViewById<ImageButton>(R.id.delete)

        val cameraInfo = getChild(groupPosition, childPosition) as CameraInfo

        playButton.setOnClickListener {
            if (isConnectedToNetwork()) {
                handleViewCamera(cameraInfo)
            } else {
                Toast.makeText(
                    context.applicationContext,
                    "Please connect to a Wifi Network",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        editButton.setOnClickListener { handleEditCamera(cameraInfo) }
        deleteButton.setOnClickListener { handleDeleteCamera(cameraInfo) }

        return view
    }

    private fun isConnectedToNetwork(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    private fun handleViewCamera(cameraInfo: CameraInfo) {
        val intent = Intent(context, ViewsActivity::class.java)
        intent.putExtra("cameraInfo", cameraInfo)
        context.startActivity(intent)
    }

    private fun handleEditCamera(cameraInfo: CameraInfo) {
        val intent = Intent(context, EditActivity::class.java)
        intent.putExtra("cameraInfo", cameraInfo)

        if (context is AppCompatActivity) {
            context.startActivityForResult(intent, editRequestCode, null)
        } else {
            context.startActivity(intent)
        }
    }


    private fun handleDeleteCamera(cameraInfo: CameraInfo) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete ${cameraInfo.cameraName}?")
        builder.setPositiveButton("No") { _, _ -> }
        builder.setNegativeButton("Yes") { _, _ ->
            val cameraName = cameraInfo.cameraName
            if (cameraName != null) {
                deleteCameraData(cameraName)
            }
        }
        builder.show()
    }

    private fun deleteCameraData(cameraName: String) {
        val dbHelper = CameraDetailsDbHelper(context)
        val db: SQLiteDatabase = dbHelper.writableDatabase

        dbHelper.use {
            val whereClause = "${CameraDetailsDbHelper.CAMERA_NAME} = ?"
            val whereArgs = arrayOf(cameraName)

            val rowsDeleted = db.delete(CameraDetailsDbHelper.TABLE_NAME, whereClause, whereArgs)

            if (rowsDeleted > 0) {
                dataChangeListener.onCameraDataChanged()
            } else {
                Toast.makeText(context, "Failed to delete Camera", Toast.LENGTH_SHORT).show()
            }
        }
    }
}