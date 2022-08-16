package com.kotofeya.mobileconfigurator.domain.user;

import java.util.ArrayList;
import java.util.List;

public class UserNull implements UserLevel{
    @Override
    public List<UserInterface> getInterfaces() {
        return new ArrayList<>();
    }

    @Override
    public UserType getUserType() {
        return UserType.USER_NULL;
    }
}
