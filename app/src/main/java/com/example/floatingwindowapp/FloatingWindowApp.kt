package com.example.floatingwindowapp
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.example.floatingwindowapp.Common.Companion.currDes

class FloatingWindowApp: Service() {

    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutParams : WindowManager.LayoutParams
    private var LAYOUT_TYPE : Int? = null
    private lateinit var windowManager: WindowManager
    private lateinit var edtDes : EditText
    private lateinit var btnMax : Button

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater =baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup
        btnMax = floatView.findViewById(R.id.btnMax)
        edtDes = floatView.findViewById(R.id.edt_des)
        edtDes.setText(currDes)
        edtDes.setSelection(edtDes.text.toString().length)
        edtDes.isCursorVisible = false

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else  LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST

        floatWindowLayoutParams = WindowManager.LayoutParams(
            (width * 0.55f).toInt(),
            (height * 0.55f).toInt(),
            LAYOUT_TYPE!!,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        floatWindowLayoutParams.gravity = Gravity.CENTER
        floatWindowLayoutParams.x =0
        floatWindowLayoutParams.y =0

        windowManager.addView(floatView, floatWindowLayoutParams)

        btnMax.setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)

            val back = Intent(this@FloatingWindowApp, MainActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(back)
        }

    }
}