package com.hipla.channel.foodModule.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*

object KeyboardHideShow {

    fun hideKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getLocalStartEndTime(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("HH:mm a")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun getDateInLocalTime(s: String): String? {
        return try {
            val offset = TimeZone.getDefault().rawOffset - TimeZone.getDefault().dstSavings
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
            val dateString = simpleDateFormat.format(s.toLong()-offset)
            return String.format(dateString)
        } catch (e: Exception) {
            e.toString()
        }
    }



    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            return connectivityManager.isDefaultNetworkActive
        }
        return false
    }

}