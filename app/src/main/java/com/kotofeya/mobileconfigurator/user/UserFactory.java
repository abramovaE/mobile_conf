package com.kotofeya.mobileconfigurator.user;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;

public class UserFactory {
    private static UserLevel user;
    private static final String USER_FULL = "full";
    private static final String USER_TRANSPORT = "transport";

    public static UserLevel getUser(){
        if(user == null){
            switch (App.get().getLevel()){
                case USER_FULL:
                    user = new UserFull();
                    break;
                case USER_TRANSPORT:
                    user = new UserTransport();
                    break;
                default:
                    user = new UserNull();
                    break;
            }
        }
        Logger.d(Logger.UTILS_LOG, "getUser(), user: " + user.getUserType() + " " + user.getInterfaces());
        return user;
    }
}
