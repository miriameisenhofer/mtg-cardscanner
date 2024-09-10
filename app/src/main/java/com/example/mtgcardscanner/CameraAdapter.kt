package com.example.mtgcardscanner

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//ss androidx.camera.camera2.interop.Camera2CameraInfo.getCameraCharacteristic(android.hardware.camera2.CameraCharacteristics.Key) with android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVE
class CameraAdapter(onTextFound: (String) -> Unit) {
    // imageAnalyzerExecutor: is an ExecutorService responsible for running the image analysis asynchonously in a new thread
    private val imageAnalyzerExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    // imageAnalyzer is an ImageAnalysis use case
    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder().build().also {
            // set imageAnalyzer's analyzer property to an instance of our ImageAnalyzer
            it.setAnalyzer(
                imageAnalyzerExecutor,
                ImageAnalyzer(onTextFound)
            )
        }
    }

    // startCamera: receive ProcessCameraProvider future and define a Runnable which will be responsible for showing the camera preview on-screen
    fun startCamera(context: Context, lifecycleOwner: LifecycleOwner, preview: Preview) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val runnable = Runnable {
            //val preview= Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
            with(cameraProviderFuture.get()) {
                unbindAll()
                // bind cameraProviderFuture to LifecycleOwner to ensure that it is lifecycle-aware
                bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            }
        }
        cameraProviderFuture.addListener(runnable, ContextCompat.getMainExecutor(context))
    }

    // shutdown() kills the imageAnalyzerExecutor and needs to be called when we no longer need to process the camera output
    fun shutdown() {
        imageAnalyzerExecutor.shutdown()
    }
}