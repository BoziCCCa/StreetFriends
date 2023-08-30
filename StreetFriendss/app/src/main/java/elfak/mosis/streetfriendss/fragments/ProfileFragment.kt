package elfak.mosis.streetfriendss.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.databinding.FragmentProfileBinding
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import elfak.mosis.streetfriendss.viewmodels.StrayAnimalViewModel
import elfak.mosis.streetfriendss.viewmodels.UsersViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private  val strayViewModel: StrayAnimalViewModel by activityViewModels()
    private  val usersViewModel: UsersViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        val loggedUser = loggedUserViewModel.user

        // Set the user's information in the layout elements
        binding.profileName.text = loggedUser?.firstName
        binding.profileLastName.text = loggedUser?.lastName
        binding.profileUsername.text = loggedUser?.username
        binding.profilePhone.text = loggedUser?.phoneNumber
        binding.profilePoints.text = loggedUser?.points.toString()

        if (loggedUser?.imageUrl != null) {
            Glide.with(this)
                .load(loggedUser?.imageUrl)
                .placeholder(R.drawable.avatar) // Placeholder image while loading // Error image if loading fails
                .into(binding.profilePhoto)
        } else {
            // If there is no profile picture URL, you can set a default image here.
            // For example:
            // binding.profil_slika.setImageResource(R.drawable.default_profile_picture)
        }

        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item=menu.findItem(R.id.action_show_profile)
        item.isVisible=false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout ->{
                val sharedPreferences =
                    requireContext().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
                true
            }
            R.id.action_show_scoreboard->{
                this.findNavController().navigate(R.id.action_profileFragment_to_leaderboardFragment)
                true
            }
            R.id.action_show_map->{
                this.findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
                true
            }
            R.id.action_list->{
                this.findNavController().navigate(R.id.action_profileFragment_to_strayListFragment)
                true
            }
            else->super.onContextItemSelected(item)
        }
    }

    companion object {

    }
}