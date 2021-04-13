package com.example.runningtrackerapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.PERSON_NAME
import com.example.runningtrackerapp.constant.Constants.PERSON_WEIGHT
import com.example.runningtrackerapp.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldsFromSharedPref()

        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSaredPref()
            if (success){
                Snackbar.make(view,"Saved successfully",Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view,"Please Enter all Fields",Snackbar.LENGTH_LONG).show()

            }
        }
    }

    private fun loadFieldsFromSharedPref(){
        val name = sharedPref.getString(PERSON_NAME,"") ?: ""
        val weight = sharedPref.getFloat(PERSON_WEIGHT,80f)

        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applyChangesToSaredPref(): Boolean{

        val nameText = etName.text.toString()
        val weightText = etWeight.text.toString()

        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(PERSON_NAME,nameText)
            .putFloat(PERSON_WEIGHT,weightText.toFloat())
            .apply()

        val toolbarText = "Let's go $nameText"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }

}