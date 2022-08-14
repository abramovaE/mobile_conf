package com.kotofeya.mobileconfigurator.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CountDownRunner implements Runnable {

    private final MainActivityViewModel viewModel;

    public CountDownRunner(MainActivityViewModel viewModel){
      this.viewModel = viewModel;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                doWork();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) { }
        }
    }

    public void doWork() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy/HH:mm:ss", Locale.getDefault());
                String currentDateTime = sdf.format(new Date());
                viewModel.setTime(currentDateTime);
            } catch (Exception e) { }
    }
}
