package com.example.floatingwindowapp

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.DEFAULT
        }
        this.serviceInfo = info
        Toast.makeText(this, "Accessibility Service Connected", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val source: AccessibilityNodeInfo? = event.source ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED, AccessibilityEvent.TYPE_VIEW_CLICKED,  -> {
                val text = source?.text?.toString()
                if (!text.isNullOrEmpty()) {
                    // Update the floating window with the captured text
                    FloatingWindowApp.updateText(text)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruptions
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Toast.makeText(this, "Accessibility Service Disconnected", Toast.LENGTH_SHORT).show()
        return super.onUnbind(intent)
    }
}
