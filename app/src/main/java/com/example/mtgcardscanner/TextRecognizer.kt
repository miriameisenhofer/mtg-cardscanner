package com.example.mtgcardscanner

import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// onTextFound: callback that will be triggered when we detect text
class TextRecognizer(private val onTextFound: (String) -> Unit) {
    fun recognizeImageText(image: Image, rotationDegrees: Int, onResult: (Boolean) -> Unit) {
        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
        // TextRecognition.getClient(): instantiate the MLKit TextRecognition API
        // call .process() and pass in the image to be processed
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(inputImage)
            .addOnSuccessListener { recognizedText ->
                processTextFromImage(recognizedText)
                onResult(true)
            }
            .addOnFailureListener { error ->
                Log.d(TAG, "Failed to recognize image text")
                error.printStackTrace()
                onResult(false)
            }
    }

    // processTextFromImage: Break down the Text object and construct a string with which to invoke onTextFound()
    private fun processTextFromImage(text: Text) {
        text.textBlocks.joinToString {
            it.text.lines().joinToString(" ")
        }.let {
            if (!it.isBlank()) {
                onTextFound(it)
            }
        }
    }

    companion object {
        private val TAG = TextRecognizer::class.java.name
    }
}