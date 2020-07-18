package com.kotofeya.mobileconfigurator;

import java.util.ArrayList;
import java.util.List;

public class Utils {


    private List<Transiver> transivers;


    public List<Transiver> getTransivers() {
        return transivers;
    }

    public Utils() {
        this.transivers = new ArrayList<>();
    }


    public Transiver getTransiverByIp(String ip){
        for(Transiver transiver: transivers){
            if(transiver.getIp().equalsIgnoreCase(ip)){
                return transiver;
            }
        }
        return null;
    }
}
