package com.viana.soundprogramming.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import com.viana.soundprogramming.util.managePermissionCamera
import java.util.*

class Camera(
        private var context: Context,
        private var surfaceView: SurfaceView,
        private val TAG: String = "Camera"
) {

    var onEachFrameListener: OnEachFrameListener? = null
    var onOpenCameraListener: OnOpenCameraListener? = null

    private var backgroundHandler = Handler(Handler.Callback {
        Log.i(TAG, it.toString())
        true
    })
    var flashLightOn = false
    var isCameraOpen = false
    private lateinit var cameraDevice: CameraDevice
    private lateinit var imageReader: ImageReader
    private lateinit var cameraSession: CameraCaptureSession
    private lateinit var captureRequest: CaptureRequest
    private val cameraManager: CameraManager = context
            .getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val bitmapReader: BitmapReader = BitmapReader(context)

    private val onImageAvailable = ImageReader
            .OnImageAvailableListener { readBitmap(it) }

    private val surfaces: MutableList<Surface> = mutableListOf()

    private fun readBitmap(reader: ImageReader) {
        val image = reader.acquireLatestImage()
        image?.let {
            val bitmap = bitmapReader.readImage(image)
            image.close()
            onEachFrameListener?.onNewFrame(bitmap)
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        if (isCameraOpen) return
        if (managePermissionCamera(context as Activity)) return
        try {
            val facingBackCameraId: String = getFacingBackCameraId() ?: return

            cameraManager.apply {
                registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
                    override fun onCameraAvailable(cameraId: String?) {
                        Log.i("CameraAvailability", "available")
                    }

                    override fun onCameraUnavailable(cameraId: String?) {
                        Log.i("CameraAvailability", "unavailable")
                    }
                }, backgroundHandler)
                openCamera(facingBackCameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        startCameraSession(camera)
                        onOpenCameraListener?.cameraOpened()
                        isCameraOpen = true
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        Log.e(TAG, "onDisconnected")
                        isCameraOpen = false
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e(TAG, "onError")
                        isCameraOpen = false
                    }

                    private fun startCameraSession(camera: CameraDevice) = try {
                        prepareImageReader()
                        createTargetSurfaces()
                        createCaptureRequest()
                        camera.createCaptureSession(surfaces,
                                object : CameraCaptureSession.StateCallback() {
                                    override fun onConfigured(session: CameraCaptureSession) {
                                        cameraSession = session
                                        startRepeatingSessionRequestToCamera()
                                    }

                                    override fun onConfigureFailed(session: CameraCaptureSession) {
                                        Log.e(TAG, "onConfigureFailed")
                                    }
                                }, backgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createTargetSurfaces() {
        surfaces.clear()
        val surface = surfaceView.holder.surface
        val imageReaderSurface = imageReader.surface
        surfaces.add(surface)
        imageReaderSurface?.let { surfaces.add(it) }
    }

    private fun createCaptureRequest() {
        cameraDevice.let {
            val captureRequestBuilder = it.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            if (flashLightOn) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
            }
            surfaces.forEach { captureRequestBuilder.addTarget(it) }
            captureRequest = captureRequestBuilder.build()
        }
    }

    private fun startRepeatingSessionRequestToCamera() {
        val value: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {}
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    if (isCameraOpen) {
                        cameraSession.capture(captureRequest, value, backgroundHandler)
                    } else {
                        cancel()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0, 300)
        /*cameraSession.setRepeatingRequest(captureRequest,
                value, backgroundHandler)*/
    }

    private fun prepareImageReader() {
        imageReader = ImageReader.newInstance(surfaceView.width, surfaceView.height,
                ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener(onImageAvailable, backgroundHandler)
    }

    fun closeCamera() = try {
        imageReader.close()
        cameraSession.abortCaptures()
        cameraSession.stopRepeating()
        cameraDevice.close()
        isCameraOpen = false
    } catch (e: Exception) {
        e.printStackTrace()
    }

    @Throws(CameraAccessException::class)
    private fun getFacingBackCameraId(): String? {
        var facingBackCameraId: String? = null
        cameraManager.let {
            val cameraIdList = it.cameraIdList
            if (cameraIdList.isNotEmpty()) {
                for (id in cameraIdList) {
                    val facing = it.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING)
                    if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        facingBackCameraId = id
                    }
                }
            }
        }
        return facingBackCameraId
    }
}

interface OnEachFrameListener {
    fun onNewFrame(bitmap: Bitmap)
}

interface OnOpenCameraListener {
    fun cameraOpened()
}