package com.hipla.channel.common

import android.util.Log
import android.view.View
import com.hipla.channel.common.LogConstant.APP_EXCEPTION
import com.hipla.channel.entity.SalesUser

object Utils {

    fun getSampleSalesUserList(): List<SalesUser> {
        return mutableListOf<SalesUser>().apply {
            add(
                SalesUser(
                    "Jonny\n" +
                            "lukose", profileImage = "https://source.unsplash.com/random/300x200"
                )
            )
            add(
                SalesUser(
                    "Marsha\n" +
                            "Thomson", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Raghav\n" +
                            "Vasumati", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Nishad\n" +
                            "Kabir", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Monica\n" +
                            "Thomas", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Ramavatar\n" +
                            "Satya", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Shiva", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Sukriti\n" +
                            "Long name Long name",
                    profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
            add(
                SalesUser(
                    "Ashraf\n" +
                            "Sangita", profileImage = "https://source.unsplash.com/random/300x200"
                )
            );
        }
    }

    inline fun tryCatch(block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: Exception) {
            Log.e(APP_EXCEPTION, e.toString())
        }
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

}
