package elfak.mosis.streetfriendss.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.classes.StrayAnimal
import elfak.mosis.streetfriendss.classes.User
import elfak.mosis.streetfriendss.databinding.FragmentAddStrayBinding
import elfak.mosis.streetfriendss.databinding.FragmentHomeBinding
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import elfak.mosis.streetfriendss.viewmodels.StrayAnimalViewModel
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Date
import kotlin.random.Random

class AddStrayFragment : Fragment() {


    private lateinit var progressDialog: ProgressDialog
    private var _binding: FragmentAddStrayBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri?=null
    private  val strayViewModel:StrayAnimalViewModel by activityViewModels()
    private val random = Random(System.currentTimeMillis())
    private var databaseUser: DatabaseReference?=null
    private var type:String?=null
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Dodavanje: 0 %")
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = 100

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }
    private fun getTodayDate(): Date {
        return Date() // This will create a Date object representing the current date and time
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout ->{
                val sharedPreferences =
                    requireContext().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(R.id.action_addStrayFragment_to_loginFragment)
                true
            }
            R.id.action_show_map->{
                this.findNavController().navigate(R.id.action_addStrayFragment_to_homeFragment)
                true
            }
            R.id.action_show_scoreboard->{
                this.findNavController().navigate(R.id.action_addStrayFragment_to_leaderboardFragment)
                true
            }
            R.id.action_list->{
                this.findNavController().navigate(R.id.action_addStrayFragment_to_strayListFragment)
                true
            }
            else->super.onContextItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = loggedUserViewModel.user
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonCat -> {
                    type="Macka"
                }
                R.id.radioButtonDog -> {
                    type="Pas"
                }
            }
        }
        binding.btnAddImage.setOnClickListener{
            openGallery()
        }
        binding.buttonDodaj.setOnClickListener{
            addStray()
        }
    }
    @SuppressLint("SuspiciousIndentation")
    private fun addStray()
    {
        val name=binding.editTextName.text.toString()
        val desc=binding.editTextDescription.text.toString()

        if(selectedImageUri!=null && name!="" && desc!="" && type!=null)
        {
            databaseUser =
                FirebaseDatabase.getInstance().getReference("Strays")
            val storageRef= FirebaseStorage.getInstance().getReference();
            val stringBuilder = StringBuilder()
            if (selectedImageUri!=null) {
                progressDialog.show()
                val fileRef = storageRef.child("strays/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val activityObj: Activity? = this.activity

                            val date = getTodayDate()
                            val ownerUsername=loggedUserViewModel.user?.username
                            val lat=loggedUserViewModel.location?.latitude
                            val lon=loggedUserViewModel.location?.longitude
                            val userKey = databaseUser?.push()?.key
                            val strayAnimal = StrayAnimal(
                                id=userKey!!,
                                ownerUsername = ownerUsername!!,
                                lat = lat!!,
                                lon = lon!!,
                                name = name,
                                date = date,
                                type = type!!,
                                description = desc,
                                photo = uri.toString()
                            )
                                val databaseUser = FirebaseDatabase.getInstance().getReference("Strays")
                                databaseUser.child(strayAnimal.id).get().addOnCompleteListener { task ->
                                        val dataSnapshot = task.result
                                            databaseUser.child(strayAnimal.id)?.setValue(strayAnimal)
                                                ?.addOnSuccessListener {
                                                    strayViewModel.addStray(strayAnimal,loggedUserViewModel.user!!)
                                                    Navigation.findNavController(binding.root).navigate(R.id.action_addStrayFragment_to_homeFragment)
                                                    Toast.makeText(activityObj, "Uspesno dodata lutalica", Toast.LENGTH_LONG).show()
                                                }
                                                ?.addOnFailureListener {
                                                    Toast.makeText(
                                                        activityObj,
                                                        "Bezuspesno dodavanje lutalice, pokusajte ponovo",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                }
                        }
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val percent = ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                        progressDialog.progress = percent
                        progressDialog.setMessage("Dodavanje: $percent %")
                    }
                    .addOnFailureListener {
                        val activityObj: Activity? = this.activity
                        Toast.makeText(activityObj, "Doslo je do greske prilikom uploadovanja slike", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                    }
            }
        }
        else
        {
            Toast.makeText(this.activity, "Morate uneti sva polja", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Image"),
            RegisterFragment.PICK_IMAGE_REQUEST
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterFragment.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null ) {
            selectedImageUri = data.data!!
            try
            {
                val imageStream: InputStream? = requireActivity().contentResolver.openInputStream(selectedImageUri!!)
                val selectedImageBitmap = BitmapFactory.decodeStream(imageStream)
                binding.imageViewDoggo.setImageBitmap(selectedImageBitmap)

            }
            catch(e: FileNotFoundException)
            {
                e.printStackTrace();
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddStrayBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        return binding.root
    }
    companion object {
        public const val PICK_IMAGE_REQUEST = 1
    }
}