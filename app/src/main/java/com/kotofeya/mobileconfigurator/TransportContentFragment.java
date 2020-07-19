package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid());
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);



        EditText typeTransport = view.findViewById(R.id.content_txt_0);
        typeTransport.setText(transportTransiver.getTransportType());

        EditText number = view.findViewById(R.id.content_txt_1);
        number.setText(transportTransiver.getFullNumber());

        EditText liter = view.findViewById(R.id.content_txt_2);
        liter.setText(transportTransiver.getFullNumber());

        EditText direction = view.findViewById(R.id.content_txt_3);
        direction.setText(transportTransiver.getDirection());

        Button btnRebootRasp = view.findViewById(R.id.content_btn_rasp);
        Button btnRebootStm = view.findViewById(R.id.content_btn_stm);
        Button btnContntSend = view.findViewById(R.id.content_btn_send);


        return view;
    }


}
