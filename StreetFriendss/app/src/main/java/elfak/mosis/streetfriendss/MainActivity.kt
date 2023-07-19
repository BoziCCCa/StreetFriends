package elfak.mosis.streetfriendss

import elfak.mosis.streetfriendss.R
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import elfak.mosis.streetfriendss.fragments.RegisterFragment



class MainActivity : AppCompatActivity() {

    private var lastBackPressTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastBackPressTime < 2000) {
            finish()
        } else {
            Toast.makeText(this,"Pritisnite back dugme ponovo da biste zatvorili aplikaciju",Toast.LENGTH_SHORT).show()
            lastBackPressTime = currentTime
        }

    }
}