package elfak.mosis.streetfriendss.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.classes.User
import java.io.FileNotFoundException
import java.io.InputStream
import elfak.mosis.streetfriendss.databinding.FragmentRegisterBinding
import kotlin.random.Random
import com.google.firebase.storage.StorageReference
import java.security.MessageDigest


class RegisterFragment : Fragment() {


    private lateinit var progressDialog: ProgressDialog
    private val random = Random(System.currentTimeMillis())
    private var selectedImageUri: Uri?=null
    private var databaseUser: DatabaseReference?=null
    private var _binding: FragmentRegisterBinding? = null
    private lateinit var storageRef: StorageReference
    private var downloadUrl: String?=null;
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Registrovanje: 0 %")
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddImage.setOnClickListener {
            openGallery()
        }
        binding.buttonRegister.setOnClickListener{
            register() }
        binding.textView.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_registerFragment_to_loginFragment)
        }

    }
    private fun openGallery()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null ) {
            selectedImageUri = data.data!!
            try
            {
                val imageStream: InputStream? = requireActivity().contentResolver.openInputStream(selectedImageUri!!)
                val selectedImageBitmap = BitmapFactory.decodeStream(imageStream)
                binding.imageView.setImageBitmap(selectedImageBitmap)

            }
            catch(e:FileNotFoundException)
                {
                    e.printStackTrace();

                }
        }
    }
    private fun register() {
        val editFirstName = requireView().findViewById<EditText>(R.id.editTextIme)
        val editLastName = requireView().findViewById<EditText>(R.id.editTextPrezime)
        val editUsername = requireView().findViewById<EditText>(R.id.editTextKorisnickoIme)
        val editPassword1 = requireView().findViewById<EditText>(R.id.editTextSifra)
        val editPassword2 = requireView().findViewById<EditText>(R.id.editTextSifra2)
        val editPhoneNumber = requireView().findViewById<EditText>(R.id.editTextPhoneNumber)

        val firstName = editFirstName.text.toString()
        val lastName = editLastName.text.toString()
        val username = editUsername.text.toString()
        val password1 = hashPassword(editPassword1.text.toString())
        val password2 = hashPassword(editPassword2.text.toString())
        val phoneNumber = editPhoneNumber.text.toString()

        if (firstName!="" && lastName!=""  && username!="" && password1!="" && password2!="" && phoneNumber!="" && selectedImageUri!=null )
        {
            databaseUser =
                FirebaseDatabase.getInstance().getReference("Users")

            val storageRef= FirebaseStorage.getInstance().getReference();
            val stringBuilder = StringBuilder()
            for (i in 1..50) {
                val randomDigit = random.nextInt(10)
                stringBuilder.append(randomDigit)
            }
            if (selectedImageUri!=null) {
                progressDialog.show()
                val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val activityObj: Activity? = this.activity
                            val user = User(firstName, lastName, username, password1, phoneNumber,uri.toString())
                            if (user.username != null) {
                                val databaseUser = FirebaseDatabase.getInstance().getReference("Users")
                                databaseUser.child(username).get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val dataSnapshot = task.result
                                        if (dataSnapshot.exists()) {
                                            Toast.makeText(activityObj, "Vec postoji nalog sa tim usernameom", Toast.LENGTH_LONG).show()
                                        }
                                        else
                                        {
                                            databaseUser?.child(user.username)?.setValue(user)
                                                ?.addOnSuccessListener {
                                                    Navigation.findNavController(binding.root).navigate(R.id.action_registerFragment_to_loginFragment)
                                                    Toast.makeText(activityObj, "Uspesno registrovan korisnik", Toast.LENGTH_LONG).show()
                                                }
                                                ?.addOnFailureListener {
                                                    Toast.makeText(activityObj, "Bezuspesno registrovanje", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                }
                            }else {
                                val activityObj: Activity? = this.activity
                                Toast.makeText(activityObj, "Unesite sve podatke", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val percent = ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                        progressDialog.progress = percent
                        progressDialog.setMessage("Registrovanje: $percent %")
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
            showMessage("Morate uneti sva polja")
        }
    }
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v.ushr(4)]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun clearInputs()
    {
        binding.editTextIme.text!!.clear()
        binding.editTextPrezime.text!!.clear()
        binding.editTextKorisnickoIme.text!!.clear()
        binding.editTextSifra.text!!.clear()
        binding.editTextSifra2.text!!.clear()
        binding.editTextPhoneNumber.text!!.clear()
        binding.imageView.setImageResource(R.drawable.avatar)
    }


    private fun showMessage(message: String)
    {
        val activityObj: Activity? = this.activity
        Toast.makeText(activityObj, message, Toast.LENGTH_SHORT).show()
    }
    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}