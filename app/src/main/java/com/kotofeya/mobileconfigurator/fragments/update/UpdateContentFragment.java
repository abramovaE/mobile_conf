package com.kotofeya.mobileconfigurator.fragments.update;


import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

public class UpdateContentFragment extends UpdateFragment {


//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        mainTxtLabel.setText(R.string.update_content_main_txt_label);
//
//        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE);
//        lvScanner.setAdapter(scannerAdapter);
//
//        utils.getBluetooth().stopScan(true);
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//        scan();
//        return view;
//    }

//    @Override
//    public void onTaskCompleted(Bundle result) {
//        scannerAdapter.notifyDataSetChanged();
//    }

//    @Override
//    public void onProgressUpdate(Integer downloaded) {
//    }

    @Override
    void loadUpdates() {
    }

    @Override
    void loadVersion() {
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_content_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_CONTENT_TYPE);
    }


}
