package com.kotofeya.mobileconfigurator.presentation.rv_adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;


public class RvAdapterView extends ConstraintLayout {
    public static final String TAG = RvAdapterView.class.getSimpleName();

    private final TextView ssid;
    private final TextView textItem0;
    private final TextView textItem1;
    private final TextView textItem2;
    private final TextView exp;
    private final Button expButton;

    public RvAdapterView(Context context) {
        super(context);
        inflate(getContext(), R.layout.scanner_rv_item, this);
        ssid = findViewById(R.id.scanner_lv_item_ssid);
        textItem0 = findViewById(R.id.scanner_lv_item0);
        textItem1 = findViewById(R.id.scanner_lv_item1);
        textItem2 = findViewById(R.id.scanner_lv_item2);
        exp = findViewById(R.id.scanner_lv_item_txt_exp);
        expButton = findViewById(R.id.scanner_lv_exp_btn);
    }

    public void setSsid(String ssid){
        this.ssid.setText(ssid);
    }
    public void setSsidColor(int color){
        ssid.setTextColor(color);
    }
    public void setTextItem0Color(int color){
        textItem0.setTextColor(color);
    }
    public void setTextItem0Visibility(int visibility){
        textItem0.setVisibility(visibility);
    }
    public void setTextItem1Visibility(int visibility){
        textItem1.setVisibility(visibility);
    }
    public void setTextItem2Visibility(int visibility){
        textItem2.setVisibility(visibility);
    }
    public void setTextItem0Text(String text){
        Logger.d(TAG, "setTextItem0Text(), text: " + text);
        textItem0.setText(text);
    }
    public void setTextItem1Text(String text){
        textItem1.setText(text);
    }
    public void setTextItem2Text(String text){
        textItem2.setText(text);
    }
    public void setExpBtnVisibility(int visibility){
        expButton.setVisibility(visibility);
    }
    public void setExpVisibility(){
        exp.setVisibility(exp.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }
    public void setExpText(String text){
        exp.setText(text);
    }

    public void setExpButtonText() {
        expButton.setText(exp.getVisibility() == View.GONE ? "+" : "-");
    }

    public void setExpButtonListener(View.OnClickListener listener) {
        expButton.setOnClickListener(listener);
    }
}