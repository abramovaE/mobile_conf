package com.kotofeya.mobileconfigurator.clientsHandler;

import com.kotofeya.mobileconfigurator.transivers.Transiver;

public class Client {

    private String ip;
    private String version;
//    private String takeInfo;
//    private TakeInfoFull takeInfoFull;
    private Transiver transiver;

    public Client(String ip) {
        this.ip = ip;
    }

    public Client(String ip, String version, Transiver transiver) {
        this.ip = ip;
        this.version = version;
    }


    public String getIp() {
        return ip;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Transiver getTransiver() {
        return transiver;
    }

    public void setTransiver(Transiver transiver) {
        this.transiver = transiver;
    }
}
