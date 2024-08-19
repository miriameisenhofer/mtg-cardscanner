package com.example.mtgcardscanner

import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mtgcardscanner.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val pic_id = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
    private fun initializeCameraCaptureLauncher() {
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Toast.makeText(this,"TODO", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Image capture failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun recognizeTextFromImage(uri: Uri) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(this, uri)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                displayRecognizedText(visionText.text)
            }
            .addOnFailureListener { e ->
                //handleTextRecognitionError(e)
                Toast.makeText(this,"TODO", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayRecognizedText(text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "Please repeat image capture, make sure text is visible on card!", Toast.LENGTH_LONG).show()
        } else {
            //binding.resultText.text = text
            // do something with text
        }
    }
}