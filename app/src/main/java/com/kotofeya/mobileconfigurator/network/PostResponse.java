package com.kotofeya.mobileconfigurator.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;

public class PostResponse {

    @SerializedName("command")
    @Expose
    private TakeInfoFull takeInfoFull;

    @SerializedName("properties")
    @Expose
    private TakeInfoFull command;

    public TakeInfoFull getTakeInfoFull() {
        return takeInfoFull;
    }
    public void setTakeInfoFull(TakeInfoFull takeInfoFull) {
        this.takeInfoFull = takeInfoFull;
    }
    public TakeInfoFull getCommand() {
        return command;
    }
    public void setCommand(TakeInfoFull command) {
        this.command = command;
    }
}

