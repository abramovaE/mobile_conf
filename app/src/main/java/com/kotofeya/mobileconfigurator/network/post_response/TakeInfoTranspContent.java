package com.kotofeya.mobileconfigurator.network.post_response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TakeInfoTranspContent implements Parcelable {
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("localRouteList")
    @Expose
    private String localRouteList;
    @SerializedName("incrRouteList")
    @Expose
    private Integer incrRouteList;

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getLocalRouteList() {
        return localRouteList;
    }
    public void setLocalRouteList(String localRouteList) {
        this.localRouteList = localRouteList;
    }
    public Integer getIncrRouteList() {
        return incrRouteList;
    }
    public void setIncrRouteList(Integer incrRouteList) {
        this.incrRouteList = incrRouteList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(date);
        parcel.writeString(localRouteList);
        parcel.writeInt(incrRouteList);
    }

    public static final Parcelable.Creator<TakeInfoTranspContent> CREATOR = new Parcelable.Creator<TakeInfoTranspContent>() {
        // распаковываем объект из Parcel
        public TakeInfoTranspContent createFromParcel(Parcel in) {
            return new TakeInfoTranspContent(in);
        }

        public TakeInfoTranspContent[] newArray(int size) {
            return new TakeInfoTranspContent[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private TakeInfoTranspContent(Parcel parcel) {
        date = parcel.readString();
        localRouteList = parcel.readString();
        incrRouteList = parcel.readInt();
    }

}
