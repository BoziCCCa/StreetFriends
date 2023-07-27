package elfak.mosis.streetfriendss.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import elfak.mosis.streetfriendss.databinding.FragmentHomeBinding
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.classes.StrayAnimal
import elfak.mosis.streetfriendss.classes.User
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import elfak.mosis.streetfriendss.viewmodels.StrayAnimalViewModel
import elfak.mosis.streetfriendss.viewmodels.UsersViewModel

class HomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private  val strayViewModel: StrayAnimalViewModel by activityViewModels()
    private  val usersViewModel: UsersViewModel by activityViewModels()
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var location: MutableLiveData<Location>



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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", "")

        val databaseUser = FirebaseDatabase.getInstance().getReference("Users")
        databaseUser.child(savedUsername!!).get().addOnCompleteListener { task ->
            val dataSnapshot = task.result
            val ime=dataSnapshot.child("firstName").getValue(String::class.java)?: ""
            val prezime=dataSnapshot.child("lastName").getValue(String::class.java)?: ""
            val slika=dataSnapshot.child("imageUrl").getValue(String::class.java)?: ""
            val korisnicko=dataSnapshot.child("username").getValue(String::class.java)?: ""
            val telefon=dataSnapshot.child("phoneNumber").getValue(String::class.java)?: ""
            val poeni=dataSnapshot.child("points").getValue(Int::class.java)?: 0
            val rang=dataSnapshot.child("rang").getValue(Int::class.java)?: 0
            val sifra=dataSnapshot.child("password").getValue(String::class.java)?: ""
            if (ime != null && prezime != null && slika != null && korisnicko != null
                && telefon != null && poeni != null && rang != null && sifra != null) {
                val userr = User(ime, prezime, korisnicko, sifra, telefon, slika, rang, poeni)
                loggedUserViewModel.user = userr
            } else {
            }
        }


        usersViewModel.getUsers()
        strayViewModel.fetchStrays()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.addStrayButton.setOnClickListener{
            this.findNavController().navigate(R.id.action_homeFragment_to_addStrayFragment)
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        location= MutableLiveData()
        mapFragment!!.getMapAsync{ mMap ->
            map =mMap
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL


            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isCompassEnabled = true


            strayViewModel.strays.observe(viewLifecycleOwner, Observer { strays ->
                Log.d("Home",strays.size.toString())
                mMap.clear()
                for (stray in strays) {
                    addMarkerToMap(stray)
                }
            })
            mMap.setOnInfoWindowClickListener(this)
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                return@getMapAsync
            }
            mMap.isMyLocationEnabled=true
            fusedLocationClient.lastLocation.addOnCompleteListener {location->
                if(location.result  !=null)
                {
                    lastLocation=location.result
                    val currentLatLong= LatLng(location.result.latitude, location.result.longitude)
                    loggedUserViewModel.location=currentLatLong
                    val googlePlex = CameraPosition.builder()
                        .target(currentLatLong)
                        .zoom(15f)
                        .bearing(0f)
                        .tilt(0f)
                        .build()

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)
                }
            }.addOnFailureListener{
                Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        binding.showFilterButton.setOnClickListener{
            val dialogFragment = FilterFragment()
            dialogFragment.show(parentFragmentManager, "FilterDialog")
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item=menu.findItem(R.id.action_show_map)
        item.isVisible=false
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout ->{
                val sharedPreferences =
                    requireContext().getSharedPreferences("StreetFriends", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                 this.findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                true
            }
            R.id.action_show_scoreboard->{
                this.findNavController().navigate(R.id.action_homeFragment_to_leaderboardFragment)
                true
            }
            R.id.action_list->{
                this.findNavController().navigate(R.id.action_homeFragment_to_strayListFragment)
                true
            }
            else->super.onContextItemSelected(item)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onMapReady(googleMap: GoogleMap) {
    }

    override fun onInfoWindowClick(marker: Marker)
    {
        strayViewModel.stray=marker.tag as StrayAnimal

        val dialogFragment = StrayInfoFragment()
        dialogFragment.show(parentFragmentManager, "StrayInfoDialog")
    }

    private fun addMarkerToMap(stray: StrayAnimal) {
        val latLng = LatLng(stray.lat, stray.lon)
        val customMarkerView = layoutInflater.inflate(R.layout.custom_marker_layout, null)
        val markerIconImageView = customMarkerView.findViewById<ImageView>(R.id.markerIcon)
        Glide.with(requireContext())
            .asBitmap()
            .load(stray.photo)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val targetSize = 150
                    val scaledBitmap = Bitmap.createScaledBitmap(resource, targetSize, targetSize, false)

                    val markerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                    val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title(stray.name)
                        .icon(markerIcon)

                    val marker=map?.addMarker(markerOptions)
                    marker?.tag = stray
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
}


}