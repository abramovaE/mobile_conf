package com.kotofeya.mobileconfigurator.user;

import java.util.List;

public interface UserLevel {
    List<UserInterface> getInterfaces();
    UserType getUserType();
}
