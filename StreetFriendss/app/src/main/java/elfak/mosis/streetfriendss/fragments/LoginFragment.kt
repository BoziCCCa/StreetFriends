package elfak.mosis.streetfriendss.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.databinding.FragmentLoginBinding
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import java.security.MessageDigest


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private val binding get() = _binding!!
    private lateinit var databaseUser: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_homeFragment);
        }

        binding.registerNow.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.btnLogin.setOnClickListener {
            login()
        }

    }
    private fun saveLoginState(username: String, password: String) {
        val sharedPreferences = requireContext().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("username",username ).apply()
        sharedPreferences.edit().putString("password",password ).apply()
    }
    private fun login(){
        val editUsername = binding.username
        val editSifra = binding.password
        val username = editUsername.text.toString()
        val sifra = hashPassword(editSifra.text.toString())

        if (username.isNotEmpty() && sifra.isNotEmpty()) {
            val databaseUser = FirebaseDatabase.getInstance().getReference("Users")
            databaseUser.child(username).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result
                    if (dataSnapshot.exists()) {
                        val dataSnapshot = task.result
                        val sifra2 = dataSnapshot.child("password").getValue(String::class.java)
                        if(sifra2==sifra)
                        {
                            saveLoginState(username,sifra)
                            Toast.makeText(this.activity,"Uspesno logovanje",Toast.LENGTH_SHORT).show()
                            Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_homeFragment)
                        }else{
                            Toast.makeText(this.activity,"Pogresna lozinka",Toast.LENGTH_SHORT).show()
                        }
                    } else {

                        Toast.makeText(this.activity,"Ne postoji nalog sa zadataim korisnickim imenom",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val exception = task.exception
                    exception?.message?.let { errorMessage ->
                        Log.e("Firebase", errorMessage)
                    }
                }
            }




        }else {
            val activityObj: Activity? = this.activity
            Toast.makeText(activityObj, "Unesite sve podatke", Toast.LENGTH_LONG).show()
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
    override fun onDestroyView() {
        super.onDestroyView()
        // Restore the visibility of the toolbar when the fragment is destroyed
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        _binding = null
    }
    companion object {


    }
}