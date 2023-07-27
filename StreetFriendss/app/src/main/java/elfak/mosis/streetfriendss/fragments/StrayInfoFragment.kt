package elfak.mosis.streetfriendss.fragments

import android.content.ContentValues
import android.graphics.Color
import android.graphics.Typeface
import android.media.Rating
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.classes.Review
import elfak.mosis.streetfriendss.classes.User
import elfak.mosis.streetfriendss.databinding.FragmentStrayInfoBinding
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import elfak.mosis.streetfriendss.viewmodels.StrayAnimalViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class StrayInfoFragment : DialogFragment() {
    private var _binding: FragmentStrayInfoBinding? = null
    private val binding get() = _binding!!
    private var userRating : Int=0

    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private  val strayViewModel: StrayAnimalViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        }

    override fun onStart() {
        super.onStart()

        // Set the dialog width and height here (optional)
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStrayInfoBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formatted= formatter.format(strayViewModel.stray?.date)
        binding.textViewStrayDate.text = formatted
        binding.textViewStrayDescription.text = strayViewModel.stray?.description
        binding.textViewStrayNameAndType.text="${strayViewModel.stray?.name} (${strayViewModel.stray?.type})"

        return binding.root
    }

    fun onStarClick(view: View) {
        val clickedStar = view as ImageView
        val star1 = binding.star1
        val star2 = binding.star2
        val star3 = binding.star3
        val star4 = binding.star4
        val star5 = binding.star5

        star1.setImageResource(R.drawable.baseline_star_border_24)
        star2.setImageResource(R.drawable.baseline_star_border_24)
        star3.setImageResource(R.drawable.baseline_star_border_24)
        star4.setImageResource(R.drawable.baseline_star_border_24)
        star5.setImageResource(R.drawable.baseline_star_border_24)

        when (clickedStar.id) {
            R.id.star5 -> {
                userRating = 5
                star5.setImageResource(R.drawable.baseline_star_24)
                star4.setImageResource(R.drawable.baseline_star_24)
                star3.setImageResource(R.drawable.baseline_star_24)
                star2.setImageResource(R.drawable.baseline_star_24)
                star1.setImageResource(R.drawable.baseline_star_24)
            }
            R.id.star4 -> {
                userRating = 4
                star4.setImageResource(R.drawable.baseline_star_24)
                star3.setImageResource(R.drawable.baseline_star_24)
                star2.setImageResource(R.drawable.baseline_star_24)
                star1.setImageResource(R.drawable.baseline_star_24)
            }
            R.id.star3 -> {
                userRating = 3
                star3.setImageResource(R.drawable.baseline_star_24)
                star2.setImageResource(R.drawable.baseline_star_24)
                star1.setImageResource(R.drawable.baseline_star_24)
            }
            R.id.star2 -> {
                userRating = 2
                star2.setImageResource(R.drawable.baseline_star_24)
                star1.setImageResource(R.drawable.baseline_star_24)
            }
            R.id.star1 -> {
                userRating = 1
                star1.setImageResource(R.drawable.baseline_star_24)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView = view?.findViewById<ImageView>(R.id.imageViewStrayPhoto)

        Glide.with(this)
            .load(strayViewModel.stray?.photo)
            .into(imageView!!)
        binding.star1.setOnClickListener{
            onStarClick(it)
        }
        binding.star2.setOnClickListener{
            onStarClick(it)
        }
        binding.star3.setOnClickListener{
            onStarClick(it)
        }
        binding.star4.setOnClickListener{
            onStarClick(it)
        }
        binding.star5.setOnClickListener{
            onStarClick(it)
        }
        binding.addCommentButton.setOnClickListener{
            addComment()
        }
        val strayList=strayViewModel.getReviewsForAnimal()
        displayListData(strayList)
    }
    private fun displayListData(dataList: List<Review>) {
        val reviewsLayout = binding.reviewsLayout
        reviewsLayout?.removeAllViews()
        for (review in dataList) {
            // Create a vertical LinearLayout for each review
            val reviewLayout = LinearLayout(requireContext())
            reviewLayout.orientation = LinearLayout.VERTICAL
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            layoutParams.setMargins(0,20,0,0)
            reviewLayout.layoutParams=layoutParams

            val userTextView = TextView(requireContext())
            userTextView.text = review.ownerUsername
            userTextView.textSize = 16f
            userTextView.setTypeface(null, Typeface.BOLD)
            reviewLayout.addView(userTextView)

            val ratingCommentLayout = LinearLayout(requireContext())
            ratingCommentLayout.orientation = LinearLayout.VERTICAL


            val ratingTextView = TextView(requireContext())
            ratingTextView.text = "Ocena: ${review.rating}"
            ratingTextView.textSize = 14f
            ratingCommentLayout.addView(ratingTextView)


            val separator = View(requireContext())
            separator.layoutParams = LinearLayout.LayoutParams(
                0,
                1,
                1f
            )
            separator.setBackgroundColor(Color.BLACK)
            ratingCommentLayout.addView(separator)


            val commentTextView = TextView(requireContext())
            commentTextView.text = "Komentar: ${review.comment}"
            commentTextView.textSize = 14f
            ratingCommentLayout.addView(commentTextView)


            reviewLayout.addView(ratingCommentLayout)

            // Add a separator between reviews
            val reviewSeparator = View(requireContext())
            reviewSeparator.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            reviewSeparator.setBackgroundColor(Color.BLACK)
            reviewLayout.addView(reviewSeparator)

            reviewsLayout?.addView(reviewLayout)
        }
    }
    private fun addComment()
    {
        var comment=binding.commentEditText.text.toString()
        if(userRating!=0 && comment!= "") {
            var points:Long=0
            var newPoints=0
            var review=Review(userRating,comment,loggedUserViewModel.user?.username!!)
            val ownerUsername= strayViewModel.stray?.ownerUsername!!

            if(userRating<3) {
                newPoints = -1
            }
            else if(userRating>=3)
                newPoints = 1

            val databaseUser = FirebaseDatabase.getInstance().getReference("Users")
            databaseUser.child(ownerUsername).get().addOnCompleteListener { task ->
                val dataSnapshot = task.result
                points=dataSnapshot.child("points").getValue(Long::class.java)!!
                val total=points+newPoints
                databaseUser.child(ownerUsername).child("points").setValue(total)
            }

            strayViewModel.addReviewToAnimal(review)
            binding.commentEditText.text?.clear()
            binding.star1.setImageResource(R.drawable.baseline_star_border_24)
            binding.star2.setImageResource(R.drawable.baseline_star_border_24)
            binding.star3.setImageResource(R.drawable.baseline_star_border_24)
            binding.star4.setImageResource(R.drawable.baseline_star_border_24)
            binding.star5.setImageResource(R.drawable.baseline_star_border_24)
            loggedUserViewModel.addPointsForComment()
            displayListData( strayViewModel.getReviewsForAnimal())
        }
        else
        {
            Toast.makeText(this.activity, "Morate odabrati rating i napisati komentar", Toast.LENGTH_SHORT).show()
        }
        }
    }


