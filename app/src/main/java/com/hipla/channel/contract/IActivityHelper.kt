package com.hipla.channel.contract

interface IActivityHelper {

    fun showLoader(message: String)

    fun dismiss()

    fun hideKeyboard()

    fun setTitle(message: String)

    fun hideTitle(message: String)

}