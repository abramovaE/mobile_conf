package com.kotofeya.mobileconfigurator;

public interface ProgressBarInt {
    default void setProgressBarVisibility(int visibility){}
//    default void setProgressBarGone(){}
    default void clearProgressBar(){}
    default void clearTextLabel(){}
}
