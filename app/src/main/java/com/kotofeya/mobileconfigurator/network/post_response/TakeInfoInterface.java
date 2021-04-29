package com.kotofeya.mobileconfigurator.network.post_response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TakeInfoInterface implements Parcelable {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("mac")
    @Expose
    private String mac;
    @SerializedName("ip")
    @Expose
    private String ip;
    @SerializedName("rentAddr")
    @Expose
    private String rentAddr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRentAddr() {
        return rentAddr;
    }

    public void setRentAddr(String rentAddr) {
        this.rentAddr = rentAddr;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mac);
        dest.writeString(ip);
        dest.writeString(rentAddr);
    }


    public static final Parcelable.Creator<TakeInfoInterface> CREATOR = new Parcelable.Creator<TakeInfoInterface>() {
        // распаковываем объект из Parcel
        public TakeInfoInterface createFromParcel(Parcel in) {
            return new TakeInfoInterface(in);
        }

        public TakeInfoInterface[] newArray(int size) {
            return new TakeInfoInterface[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private TakeInfoInterface(Parcel parcel) {
        name = parcel.readString();
        mac = parcel.readString();
        ip = parcel.readString();
        rentAddr = parcel.readString();

    }

}
