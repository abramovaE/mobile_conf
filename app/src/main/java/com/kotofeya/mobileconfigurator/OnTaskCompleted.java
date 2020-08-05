package com.kotofeya.mobileconfigurator;

import android.os.Bundle;

public interface OnTaskCompleted {
    void onTaskCompleted(Bundle result);
    void onProgressUpdate(Integer downloaded);


}
