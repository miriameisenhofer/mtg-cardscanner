package com.example.mtgcardscanner

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.mtgcardscanner.databinding.ActivityMainBinding
import com.example.mtgcardscanner.databinding.FragmentNewCollectionBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewCollectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewCollectionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentNewCollectionBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the TextView by its ID
        val textView: TextView = view.findViewById(R.id.selectedFolderText)
        val folderName = COLLECTION_FOLDER
        val updatedText = getString(R.string.current_folder_string, folderName)
        textView.text = updatedText
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewCollectionBinding.inflate(inflater, container, false)
        val view = binding.root
        val a = activity as MainActivity
        binding.chooseNewCollectionFolderButton.setOnClickListener {
            a.openFolderDialog(view)
        }
        return view
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_new_collection, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onDestroy() {
        val intent = Intent(activity, Companion::class.java)
        startActivity(intent)
        super.onDestroy()
    }

    /*private fun openFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }*/

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewCollectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewCollectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}