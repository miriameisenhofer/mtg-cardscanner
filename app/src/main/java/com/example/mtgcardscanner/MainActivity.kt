package com.example.mtgcardscanner

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.mtgcardscanner.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.Callback
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse

typealias LumaListener = (luma: Double) -> Unit

var IMAGE_ANALYSIS_ENABLED = true
var LAST_TIMESTAMP = 0L
var FOUNDCARDACTIVITY_ENABLED = true
class MainActivity : AppCompatActivity() {

    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    // Scryfall
    private lateinit var scryfallApiInterface: ScryfallApiInterface


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get card by name
        getScryfallApiInterface()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up listeners for take photo and card collection buttons
        //binding.btnTakePhoto.setOnClickListener { takePhoto() }
        binding.cardCollectionButton.setOnClickListener { viewCardCollection() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case (Exit function if use case is null, i.e. if photo button is tapped before image capture is set up)
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry (to hold the image. timestamp to create unique display name)
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MTG-CardScanner")
            }
        }

        // Create output options object which contains file + metadata (Here: Where to save -> MediaStore so other apps could display it)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

        // Set up image capture listener, which is triggered after photo has been taken (Call takePicture() on imageCapture object, pass in output options, executor and callback for when image is saved)
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object  : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                            //val savedUri = Uri.fromFile(photoFile)
                            val msg= "Photo capture succeeded: ${output.savedUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        }
            }
        )
    }

    private fun viewCardCollection() {
        Toast.makeText(baseContext, "TODO", Toast.LENGTH_SHORT).show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle of owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            //Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            // Image Capture
            imageCapture = ImageCapture.Builder().build()
            //Image Analyzer
            val imageAnalyzerExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
            val imageAnalyzer by lazy {
               ImageAnalysis.Builder().build().also { it ->
                   it.setAnalyzer(
                       imageAnalyzerExecutor,
                       //ImageAnalyzer({ Log.d(TAG, "Text Found: $it")})
                       ImageAnalyzer {
                           Log.d(TAG, "foundText: $it")
                           cleanUpCardString(
                               it,
                               findViewById<View?>(android.R.id.content).rootView
                           )
                       }
                   )
               }
            }
            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val TAG = MainActivity::class.java.name
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA
            ).apply {
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext,
                    "Permission granted",
                    Toast.LENGTH_SHORT).show()
                startCamera()
            }
        }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {
        private fun ByteBuffer.toByteArray() : ByteArray {
            rewind() // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data) // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    // Scryfall
    private fun getScryfallApiInterface() {
        scryfallApiInterface = RetrofitInstance.getInstance().create(ScryfallApiInterface::class.java)
    }

    private fun getCardImageUris(name: String, callback:(List<Uri>?) -> Unit) {
        if (!IMAGE_ANALYSIS_ENABLED || System.currentTimeMillis() - LAST_TIMESTAMP < 500) {
            //Toast.makeText(baseContext,"I_A_E = $IMAGE_ANALYSIS_ENABLED , LT = $LAST_TIMESTAMP",Toast.LENGTH_SHORT).show()
            return
        }
        LAST_TIMESTAMP = System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = scryfallApiInterface.getCardByExactName(name)
                val response= call.awaitResponse()

                if (response.isSuccessful) {
                    IMAGE_ANALYSIS_ENABLED = false
                    val card = response.body()

                    val uriList = mutableListOf<Uri>()
                    card?.imageUris?.let { uris->
                        uris.normal?.let { uriList.add(Uri.parse(it))}
                        uris.large?.let {uriList.add(Uri.parse(it))}
                    }

                    withContext(Dispatchers.Main) {
                        callback(uriList)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
    private fun getCardByName(name : String) {
        val call = scryfallApiInterface.getCardByExactName(name)
        call.enqueue(object : Callback<ScryfallCard> {
            override fun onResponse(call: Call<ScryfallCard>, response: Response<ScryfallCard>) {
                if (response.isSuccessful && response.body() != null) {
                    val card = response.body()
                    var msg= "fetched card: "
                    msg += card?.oracleText
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                }
            }

            override fun onFailure(call: Call<ScryfallCard>, t: Throwable) {
                Toast.makeText(baseContext, "FAILED TO READ", Toast.LENGTH_LONG).show()
                t.printStackTrace()
            }
        })
    }

    private fun cleanUpCardString(foundText: String, v: View) {
        if (!IMAGE_ANALYSIS_ENABLED) {
            return
        }
        val foundTextCleaned = foundText.split(", ")[0]
        //Toast.makeText(baseContext, "foundText = $foundText, cleaned = $foundTextCleaned", Toast.LENGTH_SHORT).show()
        // Get card by name
        getScryfallApiInterface()
        //getCardByName(foundTextCleaned)
        val uris = mutableListOf<Uri>()
        getCardImageUris(foundTextCleaned) {uriList ->
            if (uriList != null) {
                for (uri in uriList) {
                    uris.add(uri)
                }
                if (uris.size == uriList.size) {
                    val uriStringList = uris.map {it.toString() } as ArrayList<String>
                    val context = v.context
                    if (FOUNDCARDACTIVITY_ENABLED) {
                        FOUNDCARDACTIVITY_ENABLED = false
                        context.startActivity(
                            Intent(context, FoundCardActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putStringArrayListExtra("uriList", uriStringList)
                        )
                    }
                }
            } else {
                Log.e(TAG, "Failed to fetch card image URIs")
            }
        }
    }

}