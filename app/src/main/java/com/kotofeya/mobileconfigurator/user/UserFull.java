package com.kotofeya.mobileconfigurator.user;

import java.util.Arrays;
import java.util.List;

public class UserFull implements UserLevel {
    @Override
    public List<UserInterface> getInterfaces() {
        return Arrays.asList(UserInterface.values());
    }

    @Override
    public UserType getUserType() {
        return UserType.USER_FULL;
    }
}
