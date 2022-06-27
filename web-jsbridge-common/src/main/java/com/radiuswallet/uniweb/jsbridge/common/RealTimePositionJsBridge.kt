package com.radiuswallet.uniweb.jsbridge.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.utils.checkPermissionGranted
import com.radiuswallet.uniweb.jsbridge.common.utils.filterGrantedPermissions
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject
import timber.log.Timber
import java.lang.ref.WeakReference

open class RealTimePositionJsBridge(
    private val webViewOwner: WebViewOwner,
    private val timeOut: Long,
) : JsBridgeHandler {

    private var listener: MyLocationListener? = null
    private var readyCallBack: JsBridgeCallback? = null
    private val preferences: SharedPreferences by lazy {
        webViewOwner.context.getSharedPreferences("RealTimePositionJsBridge", Context.MODE_PRIVATE)
    }
    private var localLastLocationString: String
        get() = preferences.getString("localLastLocation", "0|0")!!
        set(value) = preferences.edit().putString("localLastLocation", value).apply()


    @SuppressLint("MissingPermission")
    private var lastLocationRunnable = Runnable {
        val lastKnownLocation = getLastKnownLocation()
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        } else {
            readyCallBack?.onCallback("0|0")
            readyCallBack = null
            resetListener()
        }
    }

    private val lastLocationHandler: Handler = Handler(Looper.getMainLooper())

    init {
        webViewOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    lastLocationHandler.removeCallbacksAndMessages(null)
                    resetListener()
                    readyCallBack = null
                    source.lifecycle.removeObserver(this)

                }
            }
        })
    }


    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (!canHandler(action)) return false
        webViewOwner.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            if (it.filterGrantedPermissions().isNotEmpty()) {
                val locationManager =
                    webViewOwner.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager != null) {
                    realRequestLocation(locationManager, callBack)
                }
            } else {
                webViewOwner.showPermissionDeniedDialog(webViewOwner.context.getString(R.string.permission_denied_position))
            }
        }
        return true
    }


    @SuppressLint("MissingPermission")
    private fun realRequestLocation(locationManager: LocationManager, callBack: JsBridgeCallback) {
        Timber.tag(TAG_WEB_LOG).e("realRequestLocation:locating")
        val providers = locationManager.getProviders(true)
        if (providers.isEmpty()) {
            callBack.onCallback(getDefaultResult())
            Timber.tag(TAG_WEB_LOG).e("realRequestLocation:no provider")
            return
        }
        val provider: String = when {
            providers.contains(LocationManager.NETWORK_PROVIDER) -> {
                LocationManager.NETWORK_PROVIDER
            }
            providers.contains(LocationManager.GPS_PROVIDER) -> {
                LocationManager.GPS_PROVIDER
            }
            providers.contains(LocationManager.PASSIVE_PROVIDER) -> {
                LocationManager.PASSIVE_PROVIDER
            }
            else -> {
                callBack.onCallback(getDefaultResult())
                Timber.tag(TAG_WEB_LOG).e("realRequestLocation:cant find provider")
                return
            }
        }
        try {
            readyCallBack = callBack
            doRealRequestLocation(locationManager, provider)
        } catch (throwable: Throwable) {
            Timber.tag(TAG_WEB_LOG).e(throwable)
        }
    }


    @SuppressLint("MissingPermission")
    private fun doRealRequestLocation(locationManager: LocationManager, provider: String) {
        listener?.let {
            locationManager.removeUpdates(it)
        }
        val myLocationListener = MyLocationListener(WeakReference(this), provider)
        Timber.tag(TAG_WEB_LOG).d("requestLocationUpdates, provider=$provider")
        lastLocationHandler.removeCallbacks(lastLocationRunnable)
        lastLocationHandler.postDelayed(lastLocationRunnable, timeOut)
        locationManager.requestLocationUpdates(
            provider,
            1000L,
            0F,
            myLocationListener
        )
        listener = myLocationListener

    }


    private fun onLocationChanged(location: Location) {
        lastLocationHandler.removeCallbacks(lastLocationRunnable)
        val latitude = location.latitude
        val longitude = location.longitude
        Timber.tag(TAG_WEB_LOG).d("定位成功： latitude=$latitude , longitude=$longitude")
        localLastLocationString = "$latitude|$longitude"
        readyCallBack?.onCallback("$latitude|$longitude")
        readyCallBack = null
        resetListener()
    }

    private class MyLocationListener(
        val reference: WeakReference<RealTimePositionJsBridge>,
        val provider: String,
    ) : LocationListener {

        override fun onLocationChanged(location: Location) {
            reference.get()?.onLocationChanged(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Timber.tag(TAG_WEB_LOG)
                .d("onStatusChanged: provider=$provider , status=$status , extras=$extras")
        }

        override fun onProviderEnabled(provider: String) {
            Timber.tag(TAG_WEB_LOG).d("onProviderEnabled: provider=$provider")
        }

        override fun onProviderDisabled(provider: String) {
            Timber.tag(TAG_WEB_LOG).d("onProviderDisabled: provider=$provider")
        }

    }

    private fun resetListener() {
        listener?.let {
            val locationManager =
                webViewOwner.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                Timber.tag(TAG_WEB_LOG).d("remove locationListener, provider=${listener?.provider}")
                locationManager.removeUpdates(it)
            }
        }
        listener = null
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val locationManager =
            webViewOwner.context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null) {
            if (webViewOwner.context.checkPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                || webViewOwner.context.checkPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                return (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
            }
        }
        return null
    }

    private fun getDefaultResult(): String {
        val lastKnownLocation = getLastKnownLocation()
        if (lastKnownLocation != null) {
            val latitude = lastKnownLocation.latitude
            val longitude = lastKnownLocation.longitude
            return "$latitude|$longitude"
        } else {
            return localLastLocationString
        }
    }

    open fun canHandler(action: String): Boolean {
        return action == ACTION_POSITION
    }

    companion object {
        private const val ACTION_POSITION = "position"

        @JvmStatic
        fun factory(timeOut: Long = 5000L): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(ACTION_POSITION) {
                RealTimePositionJsBridge(it, timeOut)
            }
        }
    }
}