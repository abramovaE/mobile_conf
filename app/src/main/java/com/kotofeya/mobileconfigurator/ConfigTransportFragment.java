package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConfigTransportFragment extends Fragment {


    private Context context;
    private Utils utils;
    private Transiver currentTransiver;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        ListView lvScanner = view.findViewById(R.id.lv_scanner);

//        List<Transiver> = BluetoothHandler.getTransivers();

        ScannerAdapter scannerAdapter = new ScannerAdapter(context, utils.getTransivers(), ScannerAdapter.CONFIG_TRANSPORT);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }


}
