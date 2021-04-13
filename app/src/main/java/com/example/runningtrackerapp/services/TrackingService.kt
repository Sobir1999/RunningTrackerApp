package com.example.runningtrackerapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.ACTION_PAUSE
import com.example.runningtrackerapp.constant.Constants.ACTION_START_OR_RESUME
import com.example.runningtrackerapp.constant.Constants.ACTION_STOP
import com.example.runningtrackerapp.constant.Constants.ACTION_TO_TRACKING_FRAGMENT
import com.example.runningtrackerapp.constant.Constants.FASTEST_LOCATON_INTERVAL
import com.example.runningtrackerapp.constant.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningtrackerapp.constant.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningtrackerapp.constant.Constants.NOTIFICATION_ID
import com.example.runningtrackerapp.constant.Constants.UPDATE_LOCATION_INTERVAL
import com.example.runningtrackerapp.constant.Constants.UPDATE_TIMER_INTERVAL
import com.example.runningtrackerapp.ui.MainActivity
import com.example.runningtrackerapp.utility.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>


@AndroidEntryPoint
class TrackingService: LifecycleService() {

    private var isFirstRun = true
    private var killService = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<Polylines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoint.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationService(it)
        })
    }

    private fun serviceKilled(){
        killService = true
        isFirstRun = true
        postInitialValues()
        pauseService()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME ->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else {
                        startTimer()
                    }
                }
                ACTION_PAUSE ->{
                    pauseService()
                }
                ACTION_STOP ->{
                    serviceKilled()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTimerEnabled = true
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(UPDATE_TIMER_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude,location.longitude)
            pathPoint.value?.apply {
                last().add(pos)
                pathPoint.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoint.value?.apply {
        add(mutableListOf())
        pathPoint.postValue(this)
    } ?: pathPoint.postValue(mutableListOf(mutableListOf()))

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){
        if (isTracking){
            val request = LocationRequest().apply {
                interval = UPDATE_LOCATION_INTERVAL
                fastestInterval = FASTEST_LOCATON_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }

            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                result.locations.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    private fun updateNotificationService(isTracking: Boolean){
        val notificationActionText = if(isTracking) "Pause" else "Resume"

        val pendingIntent = if(isTracking){
            val pauseIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent = Intent(this,TrackingService::class.java).apply{
                action = ACTION_START_OR_RESUME
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply{
            isAccessible = true
            set(curNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }

        if (!killService){
            val action = NotificationCompat.Action
                .Builder(
                    R.drawable.ic_pause_black,
                    notificationActionText,
                    pendingIntent
                ).build()

            curNotificationBuilder = baseNotificationBuilder
                .addAction(action)

            notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())
        }
    }

    private fun startForegroundService(){

        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        timeRunInSeconds.observe(this,Observer{
            if (!killService) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility().getFormattedStopWatchTimer(it * 1000L))

                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}