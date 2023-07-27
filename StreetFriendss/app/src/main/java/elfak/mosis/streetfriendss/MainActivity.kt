package elfak.mosis.streetfriendss

import android.content.Context
import elfak.mosis.streetfriendss.R
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.maps.OnMapReadyCallback
import elfak.mosis.streetfriendss.fragments.HomeFragment
import elfak.mosis.streetfriendss.fragments.LoginFragment
import elfak.mosis.streetfriendss.fragments.RegisterFragment



class MainActivity : AppCompatActivity()  {

    private var lastBackPressTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    override fun onBackPressed() {
        val currentFragment = getCurrentFragment()
        if (currentFragment is HomeFragment || currentFragment is LoginFragment) {
            if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                finish()
            } else {
                Toast.makeText(this, "Pritisni back dugme jos jednom", Toast.LENGTH_SHORT).show()
                lastBackPressTime = System.currentTimeMillis()
            }
        } else {
            super.onBackPressed() // Allow normal back button behavior for other fragments
        }
    }
    private fun getCurrentFragment(): Fragment? {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as? NavHostFragment
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }


}