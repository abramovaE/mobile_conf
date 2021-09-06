package com.kotofeya.mobileconfigurator.fragments.config;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;


public abstract class ConfigFragment extends Fragment implements OnTaskCompleted {
    TextView mainTxtLabel;
    public Context context;
    public Utils utils;
    public ImageButton mainBtnRescan;
    protected CustomViewModel viewModel;
    RecyclerView rvScanner;
    RvAdapter rvAdapter;

    public abstract RvAdapter getRvAdapter();
    public abstract void setMainTextLabel();
    public abstract void scan();

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
        View view = inflater.inflate(R.layout.scanner_fragment_cl, container, false);
        rvScanner = view.findViewById(R.id.rv_scanner);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        utils.getNewBleScanner().stopScan();
        rvAdapter = getRvAdapter();
        rvScanner.setAdapter(rvAdapter);
        scan();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
    }

    protected void updateUI(List<Transiver> transiverList){
        rvAdapter.setObjects(transiverList);
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }
}
