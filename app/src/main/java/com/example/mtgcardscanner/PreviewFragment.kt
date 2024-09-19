package com.example.mtgcardscanner

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.mtgcardscanner.databinding.FragmentPreviewBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PreviewFragment : Fragment() {
    private lateinit var binding: FragmentPreviewBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        binding.btnNewCollection.setOnClickListener { addNewCollection() }
        binding.cardCollectionButton.setOnClickListener { loadCardCollection() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
        // Set up listeners for take photo and card collection buttons
        //binding.btnTakePhoto.setOnClickListener { takePhoto() }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

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
                            Log.d(PreviewFragment.TAG, "foundText: $it")
                            val mainActivity = activity as MainActivity
                            mainActivity.cleanUpCardString(
                                it,
                                //findViewById<View?>(android.R.id.content).rootView
                                requireView()
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
                /*cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)*/
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(PreviewFragment.TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun addNewCollection() {
        // Open Fragment for user to type in collection name
        val mainActivity = activity as MainActivity
        val fragmentTransaction = mainActivity.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.preview_view, NewCollectionFragment()).commit()
    }

    private fun loadCardCollection() {
        Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
    }

    private fun allPermissionsGranted() = MainActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(MainActivity.REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in MainActivity.REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(),
                    "Permission granted",
                    Toast.LENGTH_SHORT).show()
                startCamera()
            }
        }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case (Exit function if use case is null, i.e. if photo button is tapped before image capture is set up)
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry (to hold the image. timestamp to create unique display name)
        val name = SimpleDateFormat(MainActivity.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MTG-CardScanner")
            }
        }

        // Create output options object which contains file + metadata (Here: Where to save -> MediaStore so other apps could display it)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(requireContext().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

        // Set up image capture listener, which is triggered after photo has been taken (Call takePicture() on imageCapture object, pass in output options, executor and callback for when image is saved)
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object  : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(PreviewFragment.TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    //val savedUri = Uri.fromFile(photoFile)
                    val msg= "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(PreviewFragment.TAG, msg)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    companion object {
        private val TAG = PreviewFragment::class.java.name
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PreviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PreviewFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}