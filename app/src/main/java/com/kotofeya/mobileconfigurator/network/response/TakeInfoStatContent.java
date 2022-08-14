package com.kotofeya.mobileconfigurator.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TakeInfoStatContent implements Parcelable {

    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("shortInfo")
    @Expose
    private String shortInfo;

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getShortInfo() {
        return shortInfo;
    }
    public void setShortInfo(String shortInfo) {
        this.shortInfo = shortInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(content);
        parcel.writeString(date);
        parcel.writeString(shortInfo);
    }

    public static final Parcelable.Creator<TakeInfoStatContent> CREATOR = new Parcelable.Creator<TakeInfoStatContent>() {
        // распаковываем объект из Parcel
        public TakeInfoStatContent createFromParcel(Parcel in) {
            return new TakeInfoStatContent(in);
        }

        public TakeInfoStatContent[] newArray(int size) {
            return new TakeInfoStatContent[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private TakeInfoStatContent(Parcel parcel) {
        content = parcel.readString();
        date = parcel.readString();
        shortInfo = parcel.readString();
    }

    @Override
    public String toString() {
        return "StatContent: " +
                "content: " + content + '\n' +
                "date: " + date + '\n' +
                "shortInfo: " + shortInfo + '\n';
    }
}
