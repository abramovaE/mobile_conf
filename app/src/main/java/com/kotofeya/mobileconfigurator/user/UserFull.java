package com.kotofeya.mobileconfigurator.user;

import com.kotofeya.mobileconfigurator.Logger;

import java.util.Arrays;
import java.util.List;

public class UserFull implements UserLevel {
    @Override
    public List<UserInterface> getInterfaces() {
        Logger.d(Logger.UTILS_LOG, "getInterfaces: " + UserInterface.values());
        return Arrays.asList(UserInterface.values());
    }
    @Override
    public UserType getUserType() {
        return UserType.USER_FULL;
    }
}
