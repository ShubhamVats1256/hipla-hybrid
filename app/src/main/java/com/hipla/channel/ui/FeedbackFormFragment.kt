package com.hipla.channel.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hipla.channel.BuildConfig
import com.hipla.channel.MainActivity
import com.hipla.channel.R
import com.hipla.channel.entity.response.QuestionRequest
import com.hipla.channel.entity.response.Value
import com.hipla.channel.extension.IActivityHelper
import com.hipla.channel.extension.showToastErrorMessage
import com.hipla.channel.extension.showToastSuccessMessage
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.channel.foodModule.network.Networking
import com.hipla.channel.foodModule.repository.CommonFactory
import com.hipla.channel.foodModule.repository.CommonRepository
import com.hipla.channel.foodModule.utils.PrefUtils
import com.hipla.channel.viewmodel.FeedbackViewmodel
import java.util.ArrayList

class FeedbackFormFragment  : Fragment()  {

    lateinit var sharedPreference : PrefUtils
    private lateinit var feedbackViewModel: FeedbackViewmodel

    lateinit var radioGroup1 : RadioGroup
    lateinit var radioGroup2 : RadioGroup
    lateinit var continueBtn : TextView
    lateinit var backBtn : TextView


    lateinit var etSuggestion : EditText




    private var answer_one : String = "Excellent"
    private var  answer_two : String = "Excellent"
    private var answer_three : String = "Excellent"
    private var  answer_four : String = "Excellent"
    private var answer_five : String = "Excellent"
    private var  answer_six : String = "Excellent"
    private var answer_seven : String = "Yes"
    private var answer_eight : String = ""





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = PrefUtils(requireContext())





    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        // Inflate the layout for this fragment
        val networkService = Networking.create(BuildConfig.BASE_URL,requireContext())

        if (networkService != null) {
            setUpViewModel(networkService)
        }

        requireActivity().IActivityHelper().setTitle("Feedback")
        return inflater.inflate(R.layout.fragment_feedback_form, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioGroup1 = view.findViewById(R.id.radioGroup1)

        radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            answer_one = radio.text.toString()
        }

        radioGroup2 = view.findViewById(R.id.radioGroup2)

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            answer_two = radio.text.toString()
        }

        backBtn = view.findViewById(R.id.backBtn)
        continueBtn = view.findViewById(R.id.continueBtn)
        etSuggestion = view.findViewById(R.id.etSuggestion)


        backBtn.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(intent)
        }





        continueBtn.setOnClickListener {

            answer_eight = etSuggestion.text.toString()


            var valueList: ArrayList<Value> = ArrayList()
            val quesOne = Value("How Do You Rate Response Time Of Sales?",answer_one)
            val quesTwo = Value("How Do You Rate Response Time Of CRM?",answer_two)
            val quesThree = Value(" How Would You Rate The Sample Flats?",answer_three)
            val quesFour = Value("How Would You Rate The Overall Brand Whiteland Corporation?",answer_four)
            val quesFive = Value("How Do You Rate Whiteland Projects?",answer_five)
            val quesSix = Value("How Do You Rate Our Hospitality?",answer_six)
            val quesSeven = Value("Would You Recommend Us To Your Friends And Family?",answer_seven)
            val quesEight = Value("Suggestion",answer_eight)

            valueList.add(quesOne)
            valueList.add(quesTwo)
            valueList.add(quesThree)
            valueList.add(quesFour)
            valueList.add(quesFive)
            valueList.add(quesSix)
            valueList.add(quesSeven)
            valueList.add(quesEight)



            val questionRequest =
                sharedPreference.getUserMobileNumber()
                    ?.let { it1 -> QuestionRequest(valueList, it1) }
            if (questionRequest != null) {
                feedbackViewModel.submitQuestions("Bearer " + sharedPreference.getToken().toString(), questionRequest)
            }




        }


    }




    private fun setUpViewModel(networkService : NetworkService){
        feedbackViewModel = ViewModelProvider(
            this,
            CommonFactory(CommonRepository(networkService))
        ).get(FeedbackViewmodel::class.java)


        feedbackViewModel.covidHelpSubmitData.observe(viewLifecycleOwner) {
            it?.let {
                try {
                    requireContext().showToastSuccessMessage("Feedback uploaded successfully!")

                    val intent = Intent(activity, MainActivity::class.java)
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                or Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                    startActivity(intent)
                }
                catch (e :Exception){
                    showSnackBar(e.localizedMessage)
                }
            }
        }

        feedbackViewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackBar(it)
        }

    }
    private fun showSnackBar(message: String) {
        requireContext().showToastErrorMessage(message)
    }


}