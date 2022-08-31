package com.hipla.channel.foodModule.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hipla.channel.MainActivity;


public class ExHandler extends ClassNotFoundException implements Thread.UncaughtExceptionHandler {

    private final Context myContext;
    private final Class<MainActivity> myActivityClass;

    public ExHandler(Context context, Class<MainActivity> c) {
        myContext = context;
        myActivityClass = c;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Intent intent = new Intent(myContext, myActivityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        myContext.startActivity(intent);
        //for restarting the Activity
        System.exit(0);
    }
}
