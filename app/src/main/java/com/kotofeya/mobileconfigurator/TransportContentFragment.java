package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TransportContentFragment extends Fragment {

    private Context context;
    private Utils utils;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        TransportTransiver transportTransiver = (TransportTransiver) utils.getCurrentTransiver();

        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid() + " " + transportTransiver.getTransportType() + "/" + transportTransiver.getFullNumber() + "/" + transportTransiver.getDirection());
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);

        Spinner spinner = view.findViewById(R.id.content_spn_0);
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(transportTransiver.getTransportType());
        spinner.setVisibility(View.VISIBLE);

        EditText number = view.findViewById(R.id.content_txt_1);
        number.setText(transportTransiver.getFullNumber());
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);

        EditText liter = view.findViewById(R.id.content_txt_2);
        liter.setText(transportTransiver.getFullNumber());
        liter.setHint(R.string.content_transport_litera_hint);
        liter.setVisibility(View.VISIBLE);
//

        Spinner spinnerDir = view.findViewById(R.id.content_spn_1);
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDir.setAdapter(adapterDir);
        spinnerDir.setSelection(transportTransiver.getDirection());
        spinnerDir.setVisibility(View.VISIBLE);

//        EditText direction = view.findViewById(R.id.content_txt_2);
//        direction.setText(transportTransiver.getDirection() + "");

        Button btnRebootRasp = view.findViewById(R.id.content_btn_rasp);
        Button btnRebootStm = view.findViewById(R.id.content_btn_stm);
        Button btnContntSend = view.findViewById(R.id.content_btn_send);
        btnContntSend.setEnabled(false);


        return view;
    }


}
