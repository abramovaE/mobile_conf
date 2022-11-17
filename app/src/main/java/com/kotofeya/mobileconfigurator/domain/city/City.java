package com.kotofeya.mobileconfigurator.domain.city;

import androidx.annotation.NonNull;

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
    private String fullName;

    public City() {}

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

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @NonNull
    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", index='" + index + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}