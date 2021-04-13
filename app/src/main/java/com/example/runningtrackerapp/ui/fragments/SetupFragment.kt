package com.example.runningtrackerapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.FIRST_TOGGLE_RUN
import com.example.runningtrackerapp.constant.Constants.PERSON_NAME
import com.example.runningtrackerapp.constant.Constants.PERSON_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject


@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstRun = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstRun) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController()
                .navigate(R.id.action_setupFragment_to_runFragment,savedInstanceState,navOptions)
        }
        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(),"Please enter all fields",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    fun writePersonalDataToSharedPref(): Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(PERSON_NAME,name)
            .putFloat(PERSON_WEIGHT,weight.toFloat())
            .putBoolean(FIRST_TOGGLE_RUN,false)
            .apply()
        val toolbarText = "Let's go $name"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}