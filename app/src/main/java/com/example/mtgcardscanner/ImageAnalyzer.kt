package com.example.mtgcardscanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

// Inherits from ImageAnalysis.Analyzer: indicates that it is a custom ImageAnalyzer
class ImageAnalyzer(onTextFound: (String) -> Unit) : ImageAnalysis.Analyzer {
    // onTextFound: callback that will be triggered when we detect some text
    // textRecognizer: instance of TextRecognizer
    private val textRecognizer = TextRecognizer(onTextFound)

    @ExperimentalGetImage
    // analyze(): will get called for each frame of the video feed from the camera and will pass the image to the textRecognizer
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: return
        textRecognizer.recognizeImageText(image, imageProxy.imageInfo.rotationDegrees) {
            imageProxy.close()
        }
    }
}