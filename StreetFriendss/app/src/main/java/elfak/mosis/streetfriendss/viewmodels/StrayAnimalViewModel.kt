package elfak.mosis.streetfriendss.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.streetfriendss.classes.Review
import elfak.mosis.streetfriendss.classes.StrayAnimal
import elfak.mosis.streetfriendss.classes.User
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Date


class StrayAnimalViewModel: ViewModel() {

    private val database= Firebase.database.reference
    private val storageRef = FirebaseStorage.getInstance().reference


    private val _stray= MutableLiveData<StrayAnimal?>(null)
    private val _strays=MutableLiveData<List<StrayAnimal>>(emptyList())
    private var originalStraysList: List<StrayAnimal> = emptyList()


    private fun getDistance(currentLat: Double, currentLon: Double, strayLat: Double, strayLon: Double): Double {
        val earthRadius = 6371000.0

        val currentLatRad = Math.toRadians(currentLat)
        val strayLatRad = Math.toRadians(strayLat)
        val deltaLat = Math.toRadians(strayLat - currentLat)
        val deltaLon = Math.toRadians(strayLon - currentLon)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(currentLatRad) * cos(strayLatRad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    var stray
        get() = _stray.value
        set(value) { _stray.value=value}

    val strays: LiveData<List<StrayAnimal>> get() = _strays


    fun addStray(stray: StrayAnimal, user: User)
    {
        database.child("Users").child(stray.ownerUsername).child("points").setValue(user.points+10)

    }

    fun fetchStrays() {
        val straysRef = database.child("Strays")

        straysRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val straysList = mutableListOf<StrayAnimal>()

                for (straySnapshot in snapshot.children) {
                    val stray = straySnapshot.getValue(StrayAnimal::class.java)
                    stray?.let { straysList.add(it) }
                }

                originalStraysList = straysList
                _strays.value = straysList
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun filterStrays(nameFilter: String?, typeFilter: String?, dateFilter: Date?,radiusFilter:Int?,userLat:Double?,userLon: Double?) {
        val filteredList = originalStraysList.filter { stray ->
            // Add your filtering logic here based on nameFilter, typeFilter, and dateFilter
            var includeStray = true
            if (nameFilter != null) {
                includeStray = includeStray && stray.name.contains(nameFilter, ignoreCase = true)
            }
            if (typeFilter != null) {
                includeStray = includeStray && stray.type == typeFilter
            }
            if (dateFilter != null) {
                includeStray = includeStray && stray.date >= dateFilter
            }
            if(radiusFilter!= null)
            {
                includeStray=includeStray && getDistance(userLat!!,userLon!!,stray.lat!!,stray.lon!!)<radiusFilter
            }
            includeStray
        }
        _strays.value = filteredList
    }

    fun addReviewToAnimal( review: Review) {

        val strayId = stray?.id ?: return

        val reviewRef: DatabaseReference = database.child("Strays").child(strayId).child("reviews").push()
        reviewRef.setValue(review)

        stray?.reviews?.put(reviewRef.key!!, review)
    }

    fun getReviewsForAnimal(): List<Review> {

        return stray?.reviews!!.values.toList()?: emptyList()
    }
}
