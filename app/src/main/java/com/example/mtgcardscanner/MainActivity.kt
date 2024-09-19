package com.example.mtgcardscanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.mtgcardscanner.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Callback
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse


var IMAGE_ANALYSIS_ENABLED = true
var LAST_TIMESTAMP = 0L
var FOUNDCARDACTIVITY_ENABLED = true

// Selected collection folder
var COLLECTION_FOLDER: Uri? = null
// Selected collection file
var COLLECTION_FILE: Uri? = null
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Scryfall
    private lateinit var scryfallApiInterface: ScryfallApiInterface

    // for Document Folder Browsing
    private lateinit var openDocumentTreeLauncher: ActivityResultLauncher<Intent>
    // for Document Browsing
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get card by name
        getScryfallApiInterface()

        Log.d("MyActivity", "Folder URI: $COLLECTION_FOLDER")
        // Register document browser activity result handler
        openDocumentTreeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val folderUri = result.data?.data
                folderUri?.let {
                    val flags = result.data?.flags ?: 0
                    // Persist the permission for future access
                    contentResolver.takePersistableUriPermission(
                        it,
                        flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    )
                    Log.d(TAG, "Folder URI: $folderUri")
                    COLLECTION_FOLDER = folderUri
                    val textView: TextView = findViewById(R.id.selectedFolderText)
                    val folderName = COLLECTION_FOLDER?.path?.split("/")
                        ?.lastOrNull { folderPath -> folderPath.isNotEmpty() }?.removePrefix("primary:")
                    val fNewline = "\n $folderName"
                    val updatedText = getString(R.string.current_folder_string, fNewline)
                    textView.text = updatedText
                }
            }
        }

        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result.data?.data
                fileUri?.let {
                    val flags = result.data?.flags ?: 0
                    // Persist the permission for future access
                    contentResolver.takePersistableUriPermission(
                        it,
                        flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    )
                    Log.d(TAG, "Folder URI: $fileUri")
                    COLLECTION_FILE = fileUri
                    val textView: TextView = findViewById(R.id.selectedCollectionText)
                    val fileName = COLLECTION_FILE?.path?.split("/")
                        ?.lastOrNull { folderPath -> folderPath.isNotEmpty() }
                    val updatedText = getString(R.string.selected_collection_string, fileName)
                    textView.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
                    textView.text = updatedText
                }
            }
        }

        // Load PreviewFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PreviewFragment())
            .commit()
    }

    private fun addNewCollection() {
        // Open Fragment for user to type in collection name
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.preview_view, NewCollectionFragment()).commit()
    }

    private fun openFolder() {
        val folderIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        openDocumentTreeLauncher.launch(folderIntent)
    }

    public fun browseFile(){
        val fileBrowseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        fileBrowseIntent.setType("*/*")
        openDocumentLauncher.launch(fileBrowseIntent)
    }
    public fun openFolderDialog(view: View) {
        openFolder()
        /*val textView: TextView = view.findViewById(R.id.selectedFolderText)
        val folderName = COLLECTION_FOLDER?.path?.split("/")?.filter { it.isNotEmpty() }?.lastOrNull()?.removePrefix("primary:")
        val fNnewline = "\n $folderName"
        val updatedText = getString(R.string.current_folder_string, fNnewline)
        Toast.makeText(baseContext, "uT = $updatedText",Toast.LENGTH_SHORT).show()
        textView.text = updatedText*/
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private val TAG = MainActivity::class.java.name
        public const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        public val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA
            ).apply {
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
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

    public fun cleanUpCardString(foundText: String, v: View) {
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