package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.MainActivity;

public abstract class ScannerFragment extends Fragment {
    Context context;
    Utils utils;
    ImageButton mainBtnRescan;
    TextView mainTxtLabel;
    RecyclerView rvScanner;
    RvAdapter rvAdapter;

    abstract void scan();

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment_cl, container, false);
        rvScanner = view.findViewById(R.id.rv_scanner);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        return view;
    }



}
