package com.kotofeya.mobileconfigurator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.MainActivity;

public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    TextView mainTxtLabel;

    public Context context;
    public Utils utils;

    private CheckBox showAccessPointDialog;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        mainTxtLabel.setText(R.string.main_settings_btn);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        showAccessPointDialog = view.findViewById(R.id.settings_doNotAskCheckbox);
        showAccessPointDialog.setChecked(App.get().isAskForTeneth());
        showAccessPointDialog.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.settings_doNotAskCheckbox){
            App.get().setAskForTeneth(isChecked);
        }
    }
}
