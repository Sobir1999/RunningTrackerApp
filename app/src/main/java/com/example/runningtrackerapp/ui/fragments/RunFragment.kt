package com.example.runningtrackerapp.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.adapter.RunAdapter
import com.example.runningtrackerapp.constant.Constants.REQUEST_CODE
import com.example.runningtrackerapp.constant.SortedType
import com.example.runningtrackerapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*


@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run) {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        setupRecyclerView()

        when(mainViewModel.sortedType){
            SortedType.DATE -> spFilter.setSelection(0)
            SortedType.TIME -> spFilter.setSelection(1)
            SortedType.DISTANCE -> spFilter.setSelection(2)
            SortedType.AVG_SPEED -> spFilter.setSelection(3)
            SortedType.CLORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> mainViewModel.sortRun(SortedType.DATE)
                    1 -> mainViewModel.sortRun(SortedType.TIME)
                    2 -> mainViewModel.sortRun(SortedType.DISTANCE)
                    3 -> mainViewModel.sortRun(SortedType.AVG_SPEED)
                    4 -> mainViewModel.sortRun(SortedType.CLORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }

        }

        mainViewModel.run.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun setupRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }


    fun requestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(requireContext(), "Already granted", Toast.LENGTH_SHORT).show()

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Grant this Permission")
                    builder.setMessage("Location")
                    builder.setPositiveButton("Ok", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            ActivityCompat.requestPermissions(
                                requireContext() as Activity,
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                ),
                                REQUEST_CODE
                            )
                        }
                    })

                    builder.setNegativeButton("Cancel", null)

                    val alertDialog = builder.create()
                    alertDialog.show()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_CODE
                    )
                }
            }
        } else {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(requireContext(), "Already granted", Toast.LENGTH_SHORT).show()

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                REQUEST_CODE
                            )
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (requestCode == REQUEST_CODE){
                if (grantResults.isNotEmpty() && grantResults[0] + grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Permisson Granted1", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            if (requestCode == REQUEST_CODE){
                if (grantResults.isNotEmpty() && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Permisson Granted2", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}
