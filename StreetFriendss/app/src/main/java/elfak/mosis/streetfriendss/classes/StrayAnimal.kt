package elfak.mosis.streetfriendss.classes

import java.util.Date
import java.util.UUID

data class StrayAnimal(
    val id:String ,
    val ownerUsername:String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val date: Date,
    val type:String,
    val description: String,
    var photo: String,
    var reviews: HashMap<String, Review> = HashMap()
){
    constructor() : this(
        "",
        "",
        0.0,
        0.0,
        "",
        Date(),
        "",
        "",
        "",
        HashMap()
    )
}


