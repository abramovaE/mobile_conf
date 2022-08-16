package com.kotofeya.mobileconfigurator.presentation.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;

public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private MainActivityViewModel viewModel;

    @Override
    public void onStart() {
        super.onStart();
        viewModel.setMainTxtLabel(getString(R.string.main_settings_btn));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        CheckBox showAccessPointDialog = view.findViewById(R.id.settings_doNotAskCheckbox);
        showAccessPointDialog.setChecked(App.get().isAskForTenet());
        showAccessPointDialog.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.settings_doNotAskCheckbox){
            App.get().setAskForTenet(isChecked);
        }
    }
}
