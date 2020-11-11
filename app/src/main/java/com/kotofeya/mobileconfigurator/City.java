package com.kotofeya.mobileconfigurator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class City implements Serializable {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("index")
    @Expose
    private String index;

    @SerializedName("fullname")
    @Expose
    private String fullname;


    public City() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", index='" + index + '\'' +
                ", fullname='" + fullname + '\'' +
                '}';
    }
}
