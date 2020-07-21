package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StationContentFragment extends Fragment {
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


        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid());
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);


        StatTransiver statTransiver = (StatTransiver) utils.getCurrentTransiver();

        EditText floor = view.findViewById(R.id.content_txt_0);
        floor.setText(statTransiver.getFloor());

        EditText typeZummer = view.findViewById(R.id.content_txt_1);
        typeZummer.setText("");

        EditText volumeZummer = view.findViewById(R.id.content_txt_2);
        volumeZummer.setText("");
//
//        EditText modemConfig = view.findViewById(R.id.content_txt_3);
//        modemConfig.setText("");

        Button btnRebootRasp = view.findViewById(R.id.content_btn_rasp);
        Button btnRebootStm = view.findViewById(R.id.content_btn_stm);
        Button btnContntSend = view.findViewById(R.id.content_btn_send);

        return view;
    }
}
