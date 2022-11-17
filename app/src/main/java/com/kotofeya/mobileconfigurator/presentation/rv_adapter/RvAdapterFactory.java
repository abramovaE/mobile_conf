package com.kotofeya.mobileconfigurator.presentation.rv_adapter;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.util.List;

public class RvAdapterFactory {
    public static RvAdapter getRvAdapter(RvAdapterType adapterType,
                                         List<Transceiver> objects,
                                         AdapterListener adapterListener){
        return new RvAdapter(objects, adapterListener, adapterType);
    }
}
