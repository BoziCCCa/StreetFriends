package elfak.mosis.streetfriendss.classes

data class User(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val phoneNumber: String,
    var imageUrl:String,
    var rang: Int=0,
    var points: Int=0
)
