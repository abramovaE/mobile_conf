package com.kotofeya.mobileconfigurator.presentation.activities;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UpdatingTimer implements Runnable {

    private MainActivityViewModel mainActivityViewModel;
    private LocalTime localTime;
    private Transceiver transceiver;

    public UpdatingTimer(MainActivityViewModel mainActivityViewModel,
                         Transceiver transceiver) {
        this.mainActivityViewModel = mainActivityViewModel;
        localTime = LocalTime.of(0, 0, 0, 0);
        this.transceiver = transceiver;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                doWork();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void doWork() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        localTime = localTime.plusSeconds(1L);
        mainActivityViewModel.setUpdatingTime(transceiver, localTime.format(dtf));
    }
}
