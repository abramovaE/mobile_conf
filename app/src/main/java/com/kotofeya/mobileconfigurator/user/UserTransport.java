package com.kotofeya.mobileconfigurator.user;

import java.util.ArrayList;
import java.util.List;

public class UserTransport implements UserLevel{
    @Override
    public List<UserInterface> getInterfaces() {
        List<UserInterface> userInterfaces = new ArrayList<>();
        userInterfaces.add(UserInterface.WIFI_SCANNER);
        userInterfaces.add(UserInterface.BLE_SCANNER);
        userInterfaces.add(UserInterface.SETTINGS_UPDATE_CORE);
        userInterfaces.add(UserInterface.UPDATE_OS);
        userInterfaces.add(UserInterface.UPDATE_CONTENT);
        return userInterfaces;
    }

    @Override
    public UserType getUserType() {
        return UserType.USER_TRANSPORT;
    }
}
