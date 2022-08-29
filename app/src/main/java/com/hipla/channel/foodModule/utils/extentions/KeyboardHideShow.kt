package com.hipla.sentinelvms.sentinelKt.utils.extentions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object KeyboardHideShow {

    fun hideKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View, context: Context){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun getStartEndTime(s: String): String? {
        return try {
            //val offset = TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
            sdf.timeZone = TimeZone.getDefault()
            val netDate1 = sdf.parse(Date(s.toLong()).toString())
            val sdf1 = SimpleDateFormat("HH:mm a")
            sdf1.timeZone = TimeZone.getDefault()
            sdf1.format(netDate1)
        } catch (e: Exception) {
""
        }
    }

    fun getDateInLocalTime(s: String): String? {
        return try {
            val offset = TimeZone.getDefault().rawOffset - TimeZone.getDefault().dstSavings
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val dateString = simpleDateFormat.format(s.toLong()-offset)
            return String.format(dateString)
        } catch (e: Exception) {
            e.toString()
        }
    }


    fun getDate(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
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

    fun getLocalDate(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun getTimeWithoutAMPM(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("HH:mm")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun checkTimeGreaterThan(startTime: String, endTime: String): Boolean {
        val pattern = "HH:mm"
        val sdf = SimpleDateFormat(pattern)
        try {
            val date1 = sdf.parse(startTime)
            val date2 = sdf.parse(endTime)
            return date1.before(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun getDateAndTimeInGMT(s: String): String? {
        return try {
            val offset = TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings
            val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm")
            val sdf1 = SimpleDateFormat("MM-dd-yyyy HH:mm")
            val timeInMili = sdf1.parse(s).time
            val netDate = Date(timeInMili - offset)
            sdf.format(netDate)
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