package com.anonymous.subcapture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class SubCaptureModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        private var instance: SubCaptureModule? = null
        var mediaProjection: MediaProjection? = null

        // Armazenados após permissão concedida, usados para criar a projeção dentro do serviço
        var projectionGranted: Boolean = false
        var pendingResultCode: Int = Activity.RESULT_CANCELED
        var pendingProjectionData: Intent? = null

        fun sendSubtitleEvent(subtitle: String) {
            instance?.reactContext
                ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onSubtitleCaptured", subtitle)
        }

        fun sendDebugEvent(step: String, blocks: List<String> = emptyList(), selected: String = "") {
            val payload = Arguments.createMap()
            payload.putString("step", step)
            payload.putString("selected", selected)
            val arr = Arguments.createArray()
            blocks.forEach { arr.pushString(it) }
            payload.putArray("blocks", arr)
            instance?.reactContext
                ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onCaptureDebug", payload)
        }
    }

    private val SCREEN_CAPTURE_REQUEST_CODE = 200

    override fun getName() = "SubCaptureModule"

    override fun initialize() {
        super.initialize()
        instance = this
        reactContext.addActivityEventListener(this)
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        reactContext.removeActivityEventListener(this)
        instance = null
    }

    @ReactMethod
    fun isNativeModuleLinked(promise: Promise) {
        promise.resolve(true)
    }

    @ReactMethod
    fun requestScreenCapture(promise: Promise) {
        val activity = reactContext.currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Nenhuma activity ativa.")
            return
        }
        val manager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        activity.startActivityForResult(manager.createScreenCaptureIntent(), SCREEN_CAPTURE_REQUEST_CODE)
        promise.resolve(true)
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != SCREEN_CAPTURE_REQUEST_CODE) return
        if (resultCode == Activity.RESULT_OK && data != null) {
            projectionGranted = true
            pendingResultCode = resultCode
            pendingProjectionData = data
            sendDebugEvent("Permissão de captura concedida")
        } else {
            sendDebugEvent("Permissão de captura negada")
        }
    }

    override fun onNewIntent(intent: Intent) {}

    @ReactMethod
    fun startOverlay(promise: Promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(reactContext)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${reactContext.packageName}")
            )
            reactContext.currentActivity?.startActivityForResult(intent, 0, null)
            promise.reject("PERMISSION_DENIED", "Permissão de overlay não concedida.")
            return
        }

        if (!projectionGranted || pendingProjectionData == null) {
            promise.reject("NO_PROJECTION", "Permissão de captura não concedida. Use 'Preparar Captura' primeiro.")
            return
        }

        val intent = Intent(reactContext, OverlayService::class.java).apply {
            putExtra("resultCode", pendingResultCode)
            putExtra("projectionData", pendingProjectionData)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent)
        } else {
            reactContext.startService(intent)
        }
        promise.resolve(true)
    }

    @ReactMethod
    fun stopOverlay(promise: Promise) {
        val intent = Intent(reactContext, OverlayService::class.java)
        reactContext.stopService(intent)
        promise.resolve(true)
    }

    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Int) {}
}
