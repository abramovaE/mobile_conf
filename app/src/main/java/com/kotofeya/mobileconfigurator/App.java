package com.kotofeya.mobileconfigurator;


import android.app.Application;
import android.content.Context;

import androidx.fragment.app.Fragment;

public class App extends Application {

    private static App instance;

    private Context context;
    private FragmentHandler fragmentHandler;


    public static App get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public FragmentHandler getFragmentHandler() {
        return fragmentHandler;
    }

    public void setFragmentHandler(FragmentHandler fragmentHandler) {
        this.fragmentHandler = fragmentHandler;
    }
}