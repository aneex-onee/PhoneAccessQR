package com.phoneaccessqr.app.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.phoneaccessqr.app.models.ScreenFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ScreenCaptureService : Service() {

    private val binder = LocalBinder()
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var isCapturing = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private var frameListener: ((ScreenFrame) -> Unit)? = null
    private var displayMetrics = DisplayMetrics()

    inner class LocalBinder : Binder() {
        fun getService(): ScreenCaptureService = this@ScreenCaptureService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * Initialize screen capture with MediaProjection
     */
    fun initializeCapture(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Log.d("ScreenCapture", "Initialized: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}")
    }

    /**
     * Start capturing screen frames
     */
    fun startCapture(frameListener: (ScreenFrame) -> Unit) {
        if (mediaProjection == null) {
            Log.e("ScreenCapture", "MediaProjection not initialized")
            return
        }

        this.frameListener = frameListener
        isCapturing = true
        Log.d("ScreenCapture", "Screen capture started")

        scope.launch {
            captureFrames()
        }
    }

    /**
     * Capture screen frames continuously
     */
    private suspend fun captureFrames() {
        while (isCapturing) {
            try {
                val bitmap = Bitmap.createBitmap(
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    Bitmap.Config.ARGB_8888
                )

                // Create virtual display for capturing
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "ScreenCapture",
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    bitmap.also {
                        // Compress and send frame
                        val frame = ScreenFrame(
                            timestamp = System.currentTimeMillis(),
                            frameData = compressBitmap(it),
                            width = displayMetrics.widthPixels,
                            height = displayMetrics.heightPixels,
                            rotation = 0
                        )
                        frameListener?.invoke(frame)
                    },
                    null
                )

                // Frame capture delay (adjust for performance)
                kotlinx.coroutines.delay(100) // 10 FPS
            } catch (e: Exception) {
                Log.e("ScreenCapture", "Capture error: ${e.message}")
            }
        }
    }

    /**
     * Compress bitmap to JPEG format
     */
    private fun compressBitmap(bitmap: Bitmap, quality: Int = 50): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Stop capturing screen
     */
    fun stopCapture() {
        isCapturing = false
        virtualDisplay?.release()
        virtualDisplay = null
        Log.d("ScreenCapture", "Screen capture stopped")
    }

    /**
     * Get current display metrics
     */
    fun getDisplayMetrics(): DisplayMetrics = displayMetrics

    /**
     * Pause capture temporarily
     */
    fun pauseCapture() {
        isCapturing = false
        Log.d("ScreenCapture", "Screen capture paused")
    }

    /**
     * Resume capture
     */
    fun resumeCapture() {
        isCapturing = true
        scope.launch {
            captureFrames()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
        mediaProjection?.stop()
        Log.d("ScreenCapture", "Service destroyed")
    }
}
