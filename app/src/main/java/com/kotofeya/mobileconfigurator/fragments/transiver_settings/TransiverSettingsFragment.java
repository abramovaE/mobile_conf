package com.kotofeya.mobileconfigurator.fragments.transiver_settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public abstract class TransiverSettingsFragment extends Fragment implements View.OnClickListener, PostCommand, OnTaskCompleted {
    protected final Handler myHandler = new Handler();
    protected String text;
    public Context context;
    public Utils utils;
    protected CustomViewModel viewModel;
    protected String ssid;
    protected TextView mainTxtLabel;
    protected ImageButton mainBtnRescan;

    public abstract View getView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
    public abstract void updateUIBtns(List<Transiver> transivers);
    public abstract void updateUI();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getView(inflater, container, savedInstanceState);
        this.ssid = getArguments().getString("ssid");
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUIBtns);
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.ssid = getArguments().getString("ssid");
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    public void updateText(String text){
        this.text = text;
        myHandler.post(updateRunnable);
    }
}




