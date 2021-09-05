package com.kotofeya.mobileconfigurator.rv_adapter;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public abstract class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {
    Context ctx;
    LayoutInflater lInflater;
    List<Transiver> objects;
    Utils utils;

    protected Transiver transiver;
    protected TextView ssid;
    protected TextView textItem0;
    protected TextView textItem1;
    protected TextView textItem2;
    protected TextView exp;
    protected Button expButton;
    protected ConstraintLayout linearLayout;

    public RvAdapter(Context context, Utils utils, List<Transiver> objects) {
        this.ctx = context;
        this.utils = utils;
        this.objects = objects;
        this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RvAdapterView itemView = new RvAdapterView(parent.getContext());
        itemView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        transiver = getTransiver(position);
        ssid = holder.getRvCustomView().getSsid();
        textItem0 = holder.getRvCustomView().getTextItem0();
        textItem1 = holder.getRvCustomView().getTextItem1();
        textItem2 = holder.getRvCustomView().getTextItem2();
        exp = holder.getRvCustomView().getExp();
        expButton = holder.getRvCustomView().getExpButton();
        linearLayout = holder.getRvCustomView();
        if(transiver != null) {
            ssid.setText(transiver.getSsid());
            textItem0.setText(transiver.getOsVersion());
            textItem1.setText(transiver.getStmFirmware());
            textItem2.setText(transiver.getStmBootloader());
            expButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exp.setVisibility(exp.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    protected View.OnClickListener configListener(String fragment, String ssid) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.SSID_KEY, ssid);
                App.get().getFragmentHandler().changeFragmentBundle(fragment, bundle);
            }
        };
        return onClickListener;
    }

    Transiver getTransiver(int position) {
        if(objects.size() > position)
            return objects.get(position);
        return null;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder{
        private RvAdapterView customView;
        public ViewHolder(View itemView) {
            super(itemView);
            customView = (RvAdapterView) itemView;
        }
        public RvAdapterView getRvCustomView(){
            return customView;
        }
    }

    public List<Transiver> getObjects() {
        return objects;
    }

    public void setObjects(List<Transiver> objects) {
        this.objects = objects;
    }
}