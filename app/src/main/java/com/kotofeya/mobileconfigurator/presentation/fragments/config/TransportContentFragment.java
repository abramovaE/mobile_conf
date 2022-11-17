package com.kotofeya.mobileconfigurator.presentation.fragments.config;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.ContentFragmentBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.PostInfoListener;
import com.kotofeya.mobileconfigurator.network.request.SendSshCommandListener;
import com.kotofeya.mobileconfigurator.network.request.SendSshCommandUseCase;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.List;

public class TransportContentFragment extends Fragment
        implements
        PostCommand,
        View.OnClickListener {
    private static final String TAG = TransportContentFragment.class.getSimpleName();

    private static final String SEND_TRANSPORT_CONTENT_COMMAND =
            "/usr/local/bin/call --cmd TSCFG";

    private final Handler myHandler = new Handler();
    protected ContentFragmentBinding binding;
    protected MainActivityViewModel viewModel;
    protected String ssid;
    protected View.OnKeyListener onKeyListener;
    protected AdapterView.OnItemSelectedListener onItemSelectedListener;
    protected TextWatcher textWatcher;
    protected Transceiver currentTransceiver;
    protected FragmentHandler fragmentHandler;

    Spinner spnType;
    EditText number;
    Spinner spnDir;
    EditText liter1;
    EditText liter2;
    EditText liter3;
    Transceiver transportTransceiver;

    private void setContentBtnRaspListener(){
        binding.contentBtnRasp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(getString(R.string.confirm_reboot_of) + " " + PostCommand.REBOOT_RASP);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();

                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        Thread thread = new Thread(new PostInfo(ip,
                                PostCommand.REBOOT + "_" + PostCommand.REBOOT_RASP, new PostInfoListener() {
                            @Override
                            public void postInfoSuccessful(String ip, String response) {
                                fragmentHandler.changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT,
                                        false);
                            }
                            @Override
                            public void postInfoFailed(String error) {}
                        }));
                        thread.start();
                    } else {
                        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener() {
                            @Override
                            public void sendSshCommandSuccessful(String response) {
                                Logger.d(TAG, "rebootRaspSuccessful()");
                                (requireActivity()).onBackPressed();
                            }
                            @Override
                            public void sendSshCommandFailed(String message) {
                                Logger.d(TAG, "rebootRaspFailed()");
                            }
                        }, PostCommand.REBOOT_COMMAND)).start();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {

            });
            builder.setCancelable(true);
            builder.create().show();
        });
    }
    private void setContentBtnStmListener(){
        binding.contentBtnStm.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(getString(R.string.confirm_reboot_of) + " " + PostCommand.REBOOT_STM);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();

                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        Thread thread = new Thread(new PostInfo(ip,
                                PostCommand.REBOOT + "_" + PostCommand.REBOOT_STM, new PostInfoListener() {
                            @Override
                            public void postInfoSuccessful(String ip, String response) {
                                fragmentHandler.showMessage((response.startsWith("Ok"))?
                                        getString(R.string.stm_rebooted) :
                                        "reboot stm error ");

                            }
                            @Override
                            public void postInfoFailed(String error) {}
                        }));
                        thread.start();
                    } else {
                        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener() {
                            @Override
                            public void sendSshCommandSuccessful(String response) {
                                Logger.d(TAG, "rebootStmSuccessful()");
                                if (response.contains("Tested")) {
                                    (requireActivity()).runOnUiThread(() -> {
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(requireActivity());
                                        builder1.setMessage(getString(R.string.stm_rebooted));
                                        builder1.setPositiveButton(R.string.ok, (dialog, id) -> {
                                        });
                                        builder1.setCancelable(true);
                                        builder1.show();
                                    });
                                }
                            }

                            @Override
                            public void sendSshCommandFailed(String error) {

                            }
                        }, PostCommand.REBOOT_STM_COMMAND)).start();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {

            });
            builder.setCancelable(true);
            builder.create().show();
        });
    }
    private void setContentBtnClearListener(){
        binding.contentBtnClear.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(R.string.ask_clear_the_transiver);

            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();

                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        Thread thread = new Thread(new PostInfo(ip,
                                PostCommand.ERASE_CONTENT, new PostInfoListener() {
                            @Override
                            public void postInfoSuccessful(String ip, String response) {
                                fragmentHandler.showMessage(getString(R.string.rasp_was_cleared));
                                fragmentHandler.changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT,
                                        false);
                            }
                            @Override
                            public void postInfoFailed(String error) {
                                fragmentHandler.showMessage("Clear rasp error: " + error);
                            }
                        }));
                        thread.start();
                    } else {
                        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener() {
                            @Override
                            public void sendSshCommandSuccessful(String response) {
                                (requireActivity()).runOnUiThread(() -> {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireActivity());
                                    builder1.setMessage(getString(R.string.rasp_was_cleared));
                                    builder1.setPositiveButton(R.string.ok, (dialog, id) -> {});
                                    builder1.setCancelable(true);
                                    builder1.show();
                                });
                            }

                            @Override
                            public void sendSshCommandFailed(String error) {
                                Logger.d(TAG, "clearRaspFailed()");

                            }
                        }, PostCommand.CLEAR_RASP_COMMAND)).start();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            builder.setCancelable(true);
            builder.create().show();
        });
    }
    private void setContentBtnAllListener(){
        binding.contentBtnAll.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(getString(R.string.confirm_reboot_of) + " " + PostCommand.REBOOT_ALL);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();

                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        Thread thread = new Thread(new PostInfo(ip,
                                PostCommand.REBOOT + "_" + PostCommand.REBOOT_ALL, new PostInfoListener() {
                            @Override
                            public void postInfoSuccessful(String ip, String response) {
                                fragmentHandler.showMessage(getString(R.string.all_rebooted));
                                fragmentHandler.changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT, false);
                            }
                            @Override
                            public void postInfoFailed(String error) {
                                fragmentHandler.showMessage("Reboot all error: " + error);
                            }
                        }));
                        thread.start();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            builder.setCancelable(true);
            builder.create().show();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated()");
        currentTransceiver = viewModel.getTransceiverBySsid(ssid);
        binding.spoilerBtn.setOnClickListener(v -> {
            if (v.getId() == R.id.spoilerBtn) {
                TextView contentLabel = view.findViewById(R.id.content_label);
                LinearLayout rebootLl = view.findViewById(R.id.reboot_ll);
                contentLabel.setVisibility(contentLabel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                rebootLl.setVisibility(rebootLl.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                binding.contentBtnClear.setVisibility(binding.contentBtnClear.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        viewModel.transceivers.observe(getViewLifecycleOwner(), this::updateInformers);
        setFields();
        setContentBtnRaspListener();
        setContentBtnStmListener();
        setContentBtnClearListener();
        setContentBtnAllListener();
    }


    protected void setFields() {
        transportTransceiver = viewModel.getTransceiverBySsid(ssid);
        Logger.d(TAG, "getBySsid: " + transportTransceiver);

        viewModel.setMainTxtLabel(transportTransceiver.getSsid());

        spnType = binding.contentSpn0;
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
        spnType.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);

        liter1 = binding.contentTxt1;
        liter1.setHint(R.string.content_transport_litera3_hint);
        liter1.setVisibility(View.VISIBLE);
        liter1.addTextChangedListener(textWatcher);
        liter1.setOnKeyListener(onKeyListener);

        number = binding.contentTxt2;
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(textWatcher);
        number.setOnKeyListener(onKeyListener);

        liter2 = binding.contentTxt3;
        liter2.setHint(R.string.content_transport_litera1_hint);
        liter2.setVisibility(View.VISIBLE);
        liter2.addTextChangedListener(textWatcher);
        liter2.setOnKeyListener(onKeyListener);

        liter3 = binding.contentTxt4;
        liter3.setHint(R.string.content_transport_litera2_hint);
        liter3.setVisibility(View.VISIBLE);
        liter3.addTextChangedListener(textWatcher);
        liter3.setOnKeyListener(onKeyListener);

        spnDir = binding.contentSpn1;
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDir.setAdapter(adapterDir);
        spnDir.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);
        updateBtnContentSendState();
        binding.contentBtnSend.setOnClickListener(this);
    }

    protected void updateBtnContentSendState(){
        Transceiver tr = viewModel.getTransceiverBySsid(transportTransceiver.getSsid());
        String ip = tr.getIp();
        String version = tr.getVersion();
        binding.contentBtnSend
                .setEnabled(spnType.getSelectedItemPosition() > 0 && ip != null && version != null);
    }

    public void updateFields() {}

    @Override
    public void onClick(View v) {
        Logger.d(TAG, "onClick()");
        int type = spnType.getSelectedItemPosition();
        String typeHex = type + "";
        int num = 61166;
        try {
            num = Integer.parseInt(number.getText().toString());
        } catch (NumberFormatException e){ }
        String numHex = num + "";
        String lit1 = liter1.getText().toString().toLowerCase();
        String lit2 = liter2.getText().toString().toLowerCase();
        String lit3 = liter3.getText().toString().toLowerCase();
        int dir = spnDir.getSelectedItemPosition();
        String dirHex = dir + "";

        String ip = viewModel.getTransceiverBySsid(transportTransceiver.getSsid()).getIp();
        updateTransportContent(ip, typeHex, numHex, dirHex, lit1, lit2, lit3);
    }


    private void updateTransportContent(String ip,
                                        String typeHex,
                                        String numHex,
                                        String dirHex,
                                        String lit1,
                                        String lit2,
                                        String lit3) {
        String version = transportTransceiver.getVersion();
        if(version != null){
            if (!version.equals("ssh_conn")) {
                Thread thread = new Thread(
                        new PostInfo(ip,
                                transpConfig(typeHex, lit1, numHex, lit2, lit3, dirHex),
                                new PostInfoListener() {
                                    @Override
                                    public void postInfoSuccessful(String ip, String response) {
                                        fragmentHandler.changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT,
                                                false);
                                    }
                                    @Override
                                    public void postInfoFailed(String error) {
                                        Logger.d(TAG,"postInfoFailed(): " + error);
                                    }
                                }));
                thread.start();
            } else {
                String litHex = "";
                try {
                    String lit = toHex(lit1) + toHex(lit3) + toHex(lit2);
                    litHex = "" + ((!lit.isEmpty())? Long.parseLong(lit, 16) : 0);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Logger.d(TAG, "send: " + typeHex + " " + numHex + " "
                        + dirHex + " " + lit1 + " " + lit2 + " " + lit3);
                String[] args = {typeHex, numHex, litHex, dirHex};

                String command = SEND_TRANSPORT_CONTENT_COMMAND + " " +
                        args[0] + " " + args[1] + " " + args[2] + " " + args[3];

                SendSshCommandUseCase connection =
                        new SendSshCommandUseCase(ip, new SendSshCommandListener() {
                            @Override
                            public void sendSshCommandSuccessful(String response) {

                                Logger.d(TAG, "updateContentSuccessful(): " + response);
                                if(response.contains("Tested")){
                                    Logger.d(TAG, "transport content updated");
                                    fragmentHandler.showMessage(getString(R.string.content_updated));
                                    (requireActivity()).onBackPressed();
                                }
                                refreshButtons();
                            }

                            @Override
                            public void sendSshCommandFailed(String error) {
                                Logger.d(TAG, "updateContentFailed(): " + error);
                                if(!error.contains("Connection refused")) {
                                    fragmentHandler.showMessage("Error");
                                }
                                 TransportContentFragment.this.refreshButtons();
                            }
                        }, command);
                new Thread(connection).start();
            }
        }
    }

    public String toHex(String arg) throws UnsupportedEncodingException {
        if(arg.trim().isEmpty()){
            return "00";
        }
        return String.format("%x",
                new BigInteger(1, arg.getBytes("cp1251")))
                .toUpperCase();
    }




    private void updateUI() {
        Logger.d(TAG, "update ui, ssid " + ssid + " " + currentTransceiver.getSsid());
        currentTransceiver = viewModel.getTransceiverBySsid(currentTransceiver.getSsid());
        if(!currentTransceiver.getVersion().equals(Transceiver.VERSION_UNDEFINED)) {
            binding.contentBtnRasp.setEnabled(true);
            binding.contentBtnStm.setEnabled(true);
            binding.contentBtnClear.setEnabled(true);
            binding.contentBtnSend.setEnabled(true);
            if(!currentTransceiver.getVersion().equals("ssh_conn")){
                binding.contentBtnAll.setVisibility(View.VISIBLE);
                binding.contentBtnAll.setEnabled(true);
            }
        }
        updateFields();
    }

    final Runnable updateRunnable = this::updateUI;

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        onKeyListener = (v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager in = (InputMethodManager) App.get().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
            return false;
        };

        onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnContentSendState();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnContentSendState();
            }
        };

        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateBtnContentSendState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ContentFragmentBinding.inflate(inflater, container, false);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();
        this.ssid = getArguments().getString(BundleKeys.SSID_KEY);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        viewModel.isScanning.observe(getViewLifecycleOwner(), this::updateClientsScanFinished);
        return binding.getRoot();
    }

    private void updateClientsScanFinished(Boolean aBoolean) {
        if(!aBoolean){
            viewModel.pollConnectedClients();
        }
    }



    private void updateInformers(List<Transceiver> transceivers){
        updateFields();
        Logger.d(TAG, "update informers, current ssid: " + ssid);
        Logger.d(TAG, "update informers, transceivers: " + transceivers);
        Transceiver transceiver = viewModel.getTransceiverBySsid(ssid);
        if(transceiver != null){
            Logger.d(TAG, "transceiver: " + transceiver);
            currentTransceiver = transceiver;
            if(currentTransceiver.getSsid() != null){
                updateBtnContentSendState();
                updateUI();
            }
        }
    }


    private void basicScan(){
        Logger.d(TAG, "wifi scan");
        viewModel.pollConnectedClients();
    }


    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "contentFragment onStart");
        String ssid = getArguments().getString(BundleKeys.SSID_KEY);
        currentTransceiver = viewModel.getTransceiverBySsid(ssid);
        if(!refreshButtons()){
            basicScan();
        }
        viewModel.setMainBtnRescanVisibility(View.GONE);
    }

    public boolean refreshButtons(){
        Transceiver transceiver = viewModel.getTransceiverBySsid(currentTransceiver.getSsid());
        Logger.d(TAG, "refresh buttons, currentTransceiverIp: " + transceiver.getIp());
        if(currentTransceiver.getIp() != null || transceiver.getIp() != null){
            myHandler.post(updateRunnable);
            return true;
        }
        return false;
    }

}