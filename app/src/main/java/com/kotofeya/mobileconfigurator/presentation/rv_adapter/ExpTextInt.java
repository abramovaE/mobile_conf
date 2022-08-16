package com.kotofeya.mobileconfigurator.presentation.rv_adapter;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

public interface ExpTextInt  {
   default String getExpText(Transceiver transiver){
        return "";
    }
}
