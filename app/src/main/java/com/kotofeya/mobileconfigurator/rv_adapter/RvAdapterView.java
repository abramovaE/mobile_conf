package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kotofeya.mobileconfigurator.R;


public class RvAdapterView extends ConstraintLayout {

    private TextView ssid;
    private TextView textItem0;
    private TextView textItem1;
    private TextView textItem2;
    private TextView exp;
    private Button expButton;

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

    public TextView getSsid() {
        return ssid;
    }

    public TextView getTextItem0() {
        return textItem0;
    }

    public TextView getTextItem1() {
        return textItem1;
    }

    public TextView getTextItem2() {
        return textItem2;
    }

    public TextView getExp() {
        return exp;
    }

    public Button getExpButton() {
        return expButton;
    }
}