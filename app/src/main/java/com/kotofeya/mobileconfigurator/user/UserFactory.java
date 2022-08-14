package com.kotofeya.mobileconfigurator.user;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;

public class UserFactory {
    public static final String TAG = UserFactory.class.getSimpleName();

    private static UserLevel user;
    private static final String USER_FULL = "full";
    private static final String USER_TRANSPORT = "transport";
    private static final String USER_UPDATE_CORE = "update_core";

    public static UserLevel getUser(){
        if(user == null){
            switch (App.get().getLevel()){
                case USER_FULL:
                    user = new UserFull();
                    break;
                case USER_TRANSPORT:
                    user = new UserTransport();
                    break;
                case USER_UPDATE_CORE:
                    user= new UserUpdateCore();
                default:
                    user = new UserNull();
                    break;
            }
        }
        Logger.d(TAG, "getUser(), user: " + user.getUserType() + " " + user.getInterfaces());
        return user;
    }
}
