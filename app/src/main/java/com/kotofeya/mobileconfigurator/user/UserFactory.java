package com.kotofeya.mobileconfigurator.user;

import com.kotofeya.mobileconfigurator.App;

public class UserFactory {
    private static UserLevel user;

    public static UserLevel getUser(){
        if(user == null){
            switch (App.get().getLevel()){
                case "full":
                    user = new UserFull();
                case "transport":
                    user = new UserTransport();
                default:
                    user = new UserFull();
            }
        }
        return user;
    }

    public static UserType getUserType(){
        if(user == null){
            switch (App.get().getLevel()){
                case "full":
                    user = new UserFull();
                case "transport":
                    user = new UserTransport();
                default:
                    user = new UserNull();
            }
        }
        return user.getUserType();
    }
}
