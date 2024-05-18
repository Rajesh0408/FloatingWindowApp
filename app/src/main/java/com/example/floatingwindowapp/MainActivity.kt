package com.example.floatingwindowapp

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.floatingwindowapp.Common.Companion.currDes

class MainActivity : AppCompatActivity() {
    private lateinit var dialog: AlertDialog
    private lateinit var btnMin: Button
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnMin = findViewById(R.id.btnMin)
        textView = findViewById(R.id.textView)

        // Stop the service if it is already running
        if (isServiceRunning()) {
            stopService(Intent(this@MainActivity, FloatingWindowApp::class.java))
        }

        textView.text = currDes

        btnMin.setOnClickListener {
            if (checkOverlayPermission()) {
                startService(Intent(this@MainActivity, FloatingWindowApp::class.java))
                finish()
            } else {
                requestFloatingWindowPermission()
            }
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (FloatingWindowApp::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun requestFloatingWindowPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Screen Overlay Permission Needed")
        builder.setMessage("Enable 'Display over the App' from settings")
        builder.setPositiveButton("Open Settings") { dialog, _ ->
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
        dialog = builder.create()
        dialog.show()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (checkOverlayPermission()) {
                startService(Intent(this@MainActivity, FloatingWindowApp::class.java))
                finish()
            } else {
                // Handle the case where the permission is not granted
                AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("The app cannot function without the overlay permission.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1
    }
}
