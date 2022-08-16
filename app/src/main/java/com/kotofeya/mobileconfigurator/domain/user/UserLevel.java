package com.kotofeya.mobileconfigurator.domain.user;

import java.util.List;

public interface UserLevel {
    List<UserInterface> getInterfaces();
    UserType getUserType();
}
