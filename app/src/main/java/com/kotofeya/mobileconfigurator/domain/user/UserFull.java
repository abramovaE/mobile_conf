package com.kotofeya.mobileconfigurator.domain.user;

import com.kotofeya.mobileconfigurator.Logger;

import java.util.Arrays;
import java.util.List;

public class UserFull implements UserLevel {
    public static final String TAG = UserLevel.class.getSimpleName();
    @Override
    public List<UserInterface> getInterfaces() {
        Logger.d(TAG, "getInterfaces: " + Arrays.toString(UserInterface.values()));
        return Arrays.asList(UserInterface.values());
    }
    @Override
    public UserType getUserType() {
        return UserType.USER_FULL;
    }
}
