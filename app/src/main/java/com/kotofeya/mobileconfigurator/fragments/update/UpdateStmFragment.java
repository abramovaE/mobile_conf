package com.kotofeya.mobileconfigurator.fragments.update;

import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

public class UpdateStmFragment extends UpdateFragment {

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        mainTxtLabel.setText(R.string.update_stm_main_txt_label);
//
//        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE);
//        lvScanner.setAdapter(scannerAdapter);
//
//        utils.getBluetooth().stopScan(true);
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//        loadVersion();
//        scan();
//        return view;
//    }


    @Override
    void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_stm_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE);
    }

    void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

}
