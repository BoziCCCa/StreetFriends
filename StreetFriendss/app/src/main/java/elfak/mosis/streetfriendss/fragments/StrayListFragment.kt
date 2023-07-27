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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.StrayAdapter
import elfak.mosis.streetfriendss.databinding.FragmentStrayListBinding
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import elfak.mosis.streetfriendss.viewmodels.StrayAnimalViewModel
import elfak.mosis.streetfriendss.viewmodels.UsersViewModel

class StrayListFragment : Fragment() {

    private var _binding: FragmentStrayListBinding? = null
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
        _binding = FragmentStrayListBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()


        return binding.root
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item=menu.findItem(R.id.action_list)
        item.isVisible=false
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val strayListAdapter= StrayAdapter(requireContext(),strayViewModel.strays)
        binding.strayListView.adapter=strayListAdapter

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout ->{
                val sharedPreferences =
                    requireContext().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(R.id.action_strayListFragment_to_loginFragment)
                true
            }
            R.id.action_show_scoreboard->{
                this.findNavController().navigate(R.id.action_strayListFragment_to_leaderboardFragment)
                true
            }
            R.id.action_show_map->{
                this.findNavController().navigate(R.id.action_strayListFragment_to_homeFragment)
                true
            }
            else->super.onContextItemSelected(item)
        }
    }
    companion object {
    }
}