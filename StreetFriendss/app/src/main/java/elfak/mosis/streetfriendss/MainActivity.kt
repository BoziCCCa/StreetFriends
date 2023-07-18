package elfak.mosis.streetfriendss

import elfak.mosis.streetfriendss.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import elfak.mosis.streetfriendss.fragments.RegisterFragment



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // If the app is launching for the first time, add the RegisterFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .commit()
        }
    }
}