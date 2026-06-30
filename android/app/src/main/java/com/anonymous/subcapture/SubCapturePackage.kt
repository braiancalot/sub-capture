package com.anonymous.subcapture

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class SubCapturePackage : ReactPackage {

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<*, *>> = mutableListOf()

    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> {
        return mutableListOf(SubCaptureModule(reactContext))
    }
}
