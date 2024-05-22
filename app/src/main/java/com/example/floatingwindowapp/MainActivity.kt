package com.example.floatingwindowapp

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(service.name, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun showAccessibilitySettings() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Accessibility Service Needed")
        builder.setMessage("Please enable the accessibility service for this app")
        builder.setPositiveButton("Open Settings") { dialog, _ ->
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION)
        }
        dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (checkOverlayPermission()) {
                // Now request accessibility service
                if (!isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                    showAccessibilitySettings()
                } else {
                    startService(Intent(this@MainActivity, FloatingWindowApp::class.java))
                    finish()
                }
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("The app cannot function without the overlay permission.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        } else if (requestCode == REQUEST_ACCESSIBILITY_PERMISSION) {
            if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                startService(Intent(this@MainActivity, FloatingWindowApp::class.java))
                finish()
            } else {
                Toast.makeText(this, "Accessibility Service is required for this app to function.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1
        private const val REQUEST_ACCESSIBILITY_PERMISSION = 2
    }
}

