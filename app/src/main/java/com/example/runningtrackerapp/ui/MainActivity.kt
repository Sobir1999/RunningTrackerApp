package com.example.runningtrackerapp.ui

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.ACTION_TO_TRACKING_FRAGMENT
import com.example.runningtrackerapp.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(toolbar)

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->

                when(destination.id){
                    R.id.runFragment,R.id.statisticsFragment,R.id.settingsFragment -> {
                        bottomNavigationView.visibility = View.VISIBLE
                    }else -> {
                        bottomNavigationView.visibility = View.GONE
                    }
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action == ACTION_TO_TRACKING_FRAGMENT){
            navHostFragment.findNavController().navigate(R.id.action_to_globalTrackingFragment)
        }
    }
}