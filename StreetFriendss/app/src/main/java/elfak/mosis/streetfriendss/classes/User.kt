package elfak.mosis.streetfriendss.classes

import java.util.Date

data class User(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val phoneNumber: String,
    var imageUrl:String,
    var rang: Int=0,
    var points: Int=0
){
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        0,
    )
}