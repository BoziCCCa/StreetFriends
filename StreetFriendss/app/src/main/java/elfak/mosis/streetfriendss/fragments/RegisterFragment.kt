package elfak.mosis.streetfriendss.fragments

import android.app.Activity
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
import androidx.fragment.app.Fragment
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


    private val random = Random(System.currentTimeMillis())
    private var selectedImageUri: Uri?=null
    private var databaseUser: DatabaseReference?=null
    private var _binding: FragmentRegisterBinding? = null
    private lateinit var storageRef: StorageReference
    private var downloadUrl: String?=null;
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddImage.setOnClickListener {
            openGallery()
        }
        binding.buttonRegister.setOnClickListener{
            register() }

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
        val password1 = editPassword1.text.toString()
        val password2 = editPassword2.text.toString()
        val phoneNumber = editPhoneNumber.text.toString()

        if (firstName!="" && lastName!=""  && username!="" && password1!="" && password2!="" && phoneNumber!="" && selectedImageUri!=null )
        {
            databaseUser =
                FirebaseDatabase.getInstance().getReference()

            val storageRef= FirebaseStorage.getInstance().getReference();
            val stringBuilder = StringBuilder()
            for (i in 1..50) {
                val randomDigit = random.nextInt(10)
                stringBuilder.append(randomDigit)
            }
            if (selectedImageUri!=null) {
                val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val activityObj: Activity? = this.activity
                            val user = User(firstName, lastName, username, password1, phoneNumber,uri.toString())
                            val userKey = databaseUser?.push()?.key // Generate a unique key for the new object
                            if (userKey != null) {
                                databaseUser?.child(userKey)?.setValue(user)
                                    ?.addOnSuccessListener {
                                        Toast.makeText(activityObj, "Uspesno kreiran", Toast.LENGTH_LONG).show()
                                    }
                                    ?.addOnFailureListener {
                                        Toast.makeText(activityObj, "Bezuspesno registrovanje", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                val activityObj: Activity? = this.activity
                                Toast.makeText(activityObj, "Unesite sve podatke", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        val activityObj: Activity? = this.activity
                        Toast.makeText(activityObj, "Doslo je do greske prilikom uploadovanja slike", Toast.LENGTH_LONG).show()
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
    private fun uploadToFirebase()
    {
        val storageRef= FirebaseStorage.getInstance().getReference();
        val stringBuilder = StringBuilder()
        for (i in 1..50) {
            val randomDigit = random.nextInt(10)
            stringBuilder.append(randomDigit)
        }
            if (selectedImageUri!=null) {
                val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            downloadUrl = uri.toString()
                            showMessage(uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        showMessage("Doslo je do greske prilikom uploadovanja slike")
                    }
            }
        }





    private fun showMessage(message: String)
    {
        val activityObj: Activity? = this.activity
        Toast.makeText(activityObj, message, Toast.LENGTH_SHORT).show()
    }
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}