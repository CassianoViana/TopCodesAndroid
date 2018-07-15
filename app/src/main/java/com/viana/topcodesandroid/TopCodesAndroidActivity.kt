package com.viana.topcodesandroid

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.viana.soundprogramming.R
import com.viana.soundprogramming.camera.Camera
import com.viana.soundprogramming.camera.OnEachFrameListener
import com.viana.soundprogramming.camera.OnOpenCameraListener
import com.viana.soundprogramming.util.managePermissionCamera
import kotlinx.android.synthetic.main.activity_topcodesandroid.*
import topcodes.TopCode
import java.util.*

const val REQUEST_CODE_CAMERA_PERMISSION = 100

class TopCodesAndroidActivity : AppCompatActivity() {

    private lateinit var camera: Camera
    private val topCodesReader = TopCodesReader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topcodesandroid)
        managePermissionCamera(this)
        prepareCamera()
    }

    override fun onResume() {
        super.onResume()
        startAfterDelay(1000)
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    private fun prepareCamera() {
        camera = Camera(this, surfaceView)
        camera.onOpenCameraListener = object : OnOpenCameraListener {
            override fun cameraOpened() {
                camera.onEachFrameListener = object : OnEachFrameListener {
                    override fun onNewFrame(bitmap: Bitmap) {
                        topCodesReader.read(bitmap, object : TopCodesReader.Listener {
                            override fun onRead(topCodes: MutableList<TopCode>) {
                                board.updateTopCodes(topCodes)
                            }
                        })
                    }
                }
            }
        }
    }

    private fun startAfterDelay(delay: Long) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread({
                    startCamera()
                })
            }
        }, delay)
    }

    private fun startCamera() {
        camera.flashLightOn = false
        camera.openCamera()
    }

    private fun stopCamera() {
        camera.closeCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startAfterDelay(100)
    }

}