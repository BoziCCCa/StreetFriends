package elfak.mosis.streetfriendss.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import elfak.mosis.streetfriendss.R
import elfak.mosis.streetfriendss.databinding.FragmentFilterBinding
import elfak.mosis.streetfriendss.viewmodels.LoggedUserViewModel
import elfak.mosis.streetfriendss.viewmodels.StrayAnimalViewModel
import elfak.mosis.streetfriendss.viewmodels.UsersViewModel
import java.util.Calendar
import java.util.Date


class FilterFragment : DialogFragment() {
    private var type:String?=null
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private  val strayViewModel: StrayAnimalViewModel by activityViewModels()
    private  val usersViewModel: UsersViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterButton.setOnClickListener {
            filter()
        }

        binding.radioFilter.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioFilterDog -> {
                    type = "Pas"
                }

                R.id.radioFilterCat -> {
                    type = "Macka"
                }
            }
        }
    }
    private fun filter()
    {
        val name=binding.nameEditText.text.toString()
        val radius=binding.radiusEditText.text.toString().toIntOrNull()
        var date: Date? =null
        val editDate: DatePicker = view?.findViewById(R.id.datePicker)!!
        val calendar = Calendar.getInstance()
        calendar.set(editDate.year, editDate.month, editDate.dayOfMonth)
        date = calendar.time
        if(date==Date())
            date=null
        strayViewModel.filterStrays(name,type,date,radius,loggedUserViewModel.location?.latitude,loggedUserViewModel.location?.longitude)


        dismiss()
    }
    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }



    companion object {

    }
}