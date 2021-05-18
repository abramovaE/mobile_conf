package com.kotofeya.mobileconfigurator.fragments.config;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;


public abstract class ConfigFragment extends Fragment implements OnTaskCompleted {
    ScannerAdapter scannerAdapter;
    TextView mainTxtLabel;
    public Context context;
    public Utils utils;
    public ImageButton mainBtnRescan;
    protected CustomViewModel viewModel;
    ListView lvScanner;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        setMainTextLabel();
        mainBtnRescan.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        lvScanner = view.findViewById(R.id.lv_scanner);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
//        utils.getBluetooth().stopScan(true);
        utils.getNewBleScanner().stopScan();
        scannerAdapter = getScannerAdapter();
        lvScanner.setAdapter(scannerAdapter);
        scan();
        return view;
    }

    public abstract ScannerAdapter getScannerAdapter();
    public abstract void setMainTextLabel();
    public abstract void scan();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
    }

    protected void updateUI(List<Transiver> transiverList){
        scannerAdapter.setObjects(transiverList);
        scannerAdapter.notifyDataSetChanged();
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        Logger.d(Logger.BLE_SCANNER_LOG, "onStop");
//        utils.getNewBleScanner().stopScan();
//        viewModel.clearTransivers();
//    }
}
