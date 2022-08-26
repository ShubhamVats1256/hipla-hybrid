package com.hipla.channel.foodModule.utils

import android.content.Context
import android.content.SharedPreferences

class PrefUtils(val context: Context) {
    private val PREFS_NAME = "hipla_app"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object{
        const val organizationId = "organizationId"
        const val employeeId = "employeeId"
        const val userId = "user_id"

        const val businessId = "businessId"
        const val roomId = "roomId"
        const val hideStocks = "hideStock"
        const val token = "token"
        const val apikey = "api_key"
    }

    fun saveOrgId(organizationIdd: String){
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putString(organizationId, organizationIdd)
        editor.commit()
    }



    fun saveHideStock(hideStock: Boolean){
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(hideStocks, hideStock)
        editor.commit()
    }

    fun getHideStock(): Boolean {
        return sharedPref.getBoolean(hideStocks, false)
    }


    fun saveEmployeeId(employeIdd: String){
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putString(employeeId, employeIdd)
        editor.commit()
    }

    fun getToken(): String? {
        return sharedPref.getString(token, "")
    }

    fun getEmployeeId(): String? {
        return sharedPref.getString(employeeId, "")
    }

    fun savebusinessId(businessIdd: String){
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putString(businessId, businessIdd)
        editor.commit()
    }

    fun getApiKey(): String? {
        return sharedPref.getString(apikey, "")
    }
    fun saveLoginData(apiKeyValue : String){
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(apikey, apiKeyValue)
        editor.commit()
    }


    fun getOrganizationId(): String? {
        return sharedPref.getString(organizationId, "")
    }

    fun getBusinessId(): String? {
        return sharedPref.getString(businessId, "")
    }


}