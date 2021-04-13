package com.example.runningtrackerapp.utility

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningtrackerapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog: DialogFragment() {

    private var yesListener: (() -> Unit) ?= null

    fun setYesListener(listener :() -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setIcon(R.drawable.ic_delete)
            .setTitle("Cancel the Run")
            .setMessage("Are you sure to cancel the Run and delete its all data?")
            .setPositiveButton("Yes"){ _,_ ->
                yesListener?.let { yes ->
                yes()
                }
            }
            .setNegativeButton("No"){ dialogInterface,_ ->
                dialogInterface.cancel()
            }
            .create()
    }
}