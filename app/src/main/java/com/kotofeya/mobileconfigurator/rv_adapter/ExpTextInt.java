package com.kotofeya.mobileconfigurator.rv_adapter;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

public interface ExpTextInt  {
   default String getExpText(Transceiver transiver){
        return "";
    }
}
