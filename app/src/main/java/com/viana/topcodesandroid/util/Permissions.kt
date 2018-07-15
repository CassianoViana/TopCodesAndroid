package com.viana.soundprogramming.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import com.viana.topcodesandroid.REQUEST_CODE_CAMERA_PERMISSION

fun managePermissionCamera(activity: Activity): Boolean {
    val cameraNotPermitted = ActivityCompat
            .checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    if (cameraNotPermitted) {
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_CAMERA_PERMISSION)
        return true
    }
    return false
}
