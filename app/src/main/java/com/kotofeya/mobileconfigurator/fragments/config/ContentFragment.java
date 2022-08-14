package com.kotofeya.mobileconfigurator.fragments.config;

import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.databinding.ContentFragmentBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ContentFragment extends Fragment
        implements OnTaskCompleted, PostCommand, View.OnClickListener {

    public static final String TAG = ContentFragment.class.getSimpleName();

    public static final String REBOOT_TYPE="rebootType";
    public static final String REBOOT_RASP="rasp";
    public static final String REBOOT_STM="stm";
    public static final String REBOOT_ALL="all";
    public static final String REBOOT_CLEAR="clear";

    private final Handler myHandler = new Handler();

    protected ContentFragmentBinding binding;

    protected MainActivityViewModel viewModel;
    protected String ssid;
    protected View.OnKeyListener onKeyListener;
    protected AdapterView.OnItemSelectedListener onItemSelectedListener;
    protected TextWatcher textWatcher;

    protected Transceiver currentTransceiver;

    ContentClickListener contentClickListener;
    protected FragmentHandler fragmentHandler;
//    protected ClientsHandler clientsHandler;

    private void updateUI() {
        Logger.d(TAG, "update ui, ssid " + ssid + " " + currentTransceiver.getSsid());
        currentTransceiver = viewModel.getTransceiverBySsid(currentTransceiver.getSsid());
        if(currentTransceiver.getVersion() != Transceiver.VERSION_UNDEFINED) {
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

    final Runnable updateRunnable = () -> updateUI();

    protected void showMessageAndChangeFragment(@NotNull String response, String message,
                                      String errorMessage, String fragmentTag){
        if(response.startsWith("Ok")) {
            fragmentHandler.showMessage(message);
            fragmentHandler.changeFragment(fragmentTag, false);
        } else {
            fragmentHandler.showMessage(errorMessage);
        }
    }

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

        this.ssid = getArguments().getString("ssid");

        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);

        viewModel.isScanning.observe(getViewLifecycleOwner(), this::updateClientsScanFinished);
//        this.clientsHandler = ClientsHandler.getInstance();
        return binding.getRoot();
    }

    private void updateClientsScanFinished(Boolean aBoolean) {
        if(!aBoolean){
            viewModel.pollConnectedClients();
//            clientsHandler.pollConnectedClients();
        }
    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {

//        if(!aBoolean){
//            if(scannerProgressBarTv != null) {
//                scannerProgressBarTv.setText(Utils.MESSAGE_TAKE_INFO);
//                scannerProgressBarTv.setVisibility(View.VISIBLE);
//            }
//        } else {
//            if(scannerProgressBarTv != null) {
//                scannerProgressBarTv.setVisibility(View.GONE);
//            }
//        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "on view created");
        super.onViewCreated(view, savedInstanceState);
//        viewModel.getTransceivers().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.isGetTakeInfoFinished.observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);

        currentTransceiver = viewModel.getTransceiverBySsid(ssid);

        contentClickListener = new ContentClickListener(currentTransceiver, fragmentHandler);
        contentClickListener.setListener(this);

        binding.contentBtnRasp.setOnClickListener(contentClickListener);
        binding.contentBtnStm.setOnClickListener(contentClickListener);
        binding.contentBtnClear.setOnClickListener(contentClickListener);
        binding.contentBtnAll.setOnClickListener(contentClickListener);
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
    }


    protected abstract void setFields();

    private void updateInformers(List<Transceiver> transceivers){
        updateFields();
        Logger.d(TAG, "update informers, current ssid: " + ssid);
        Logger.d(TAG, "update informers, transceivers: " + transceivers);
        Transceiver transiver = viewModel.getTransceiverBySsid(ssid);
        if(transiver != null){
            Logger.d(TAG, "transceiver: " + transiver);
            currentTransceiver = transiver;
            if(currentTransceiver.getSsid() != null){
                updateBtnContentSendState();
                updateUI();
            }
        }
    }


    protected abstract void updateBtnContentSendState();

    @Override
    public void onTaskCompleted(Bundle result) {

        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String resultStr = result.getString(BundleKeys.RESULT_KEY);
        Logger.d(TAG, "on task completed, resultCode: " + resultCode);

        if(resultCode == TaskCode.REBOOT_STM_CODE && resultStr.contains("Tested")){
            Logger.d(TAG, "stm rebooted");
            fragmentHandler.showMessage(getString(R.string.stm_rebooted));
        }
        else if(resultCode == TaskCode.REBOOT_CODE){
            (requireActivity()).onBackPressed();
        }
        else if(resultCode == TaskCode.CLEAR_RASP_CODE){
            Logger.d(TAG, "rasp was cleared");
            fragmentHandler.showMessage(getString(R.string.rasp_was_cleared));
        }
        else if(resultCode == TaskCode.SSH_ERROR_CODE || resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            if(!resultStr.contains("Connection refused")) {
                Logger.d(TAG, "Connection refused, error");
                fragmentHandler.showMessage("Error");
            }
        }
        else if(resultCode == TaskCode.SEND_TRANSPORT_CONTENT_CODE && resultStr.contains("Tested")){
            Logger.d(TAG, "transport content updated");
            fragmentHandler.showMessage(getString(R.string.content_updated));
            (requireActivity()).onBackPressed();
        }
        else if(resultCode == TaskCode.SEND_STATION_CONTENT_CODE && resultStr.contains("Tested")){
            Logger.d(TAG, "station content updated");
            fragmentHandler.showMessage(getString(R.string.content_updated));
            (requireActivity()).onBackPressed();
        }
        refreshButtons();
    }

    private void basicScan(){
        Logger.d(TAG, "wifi scan");
        viewModel.pollConnectedClients();
//        clientsHandler.pollConnectedClients();
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

    private boolean refreshButtons(){
        Transceiver transceiver = viewModel.getTransceiverBySsid(currentTransceiver.getSsid());
        Logger.d(TAG, "refresh buttons, currentTransceiverIp: " + transceiver.getIp());
        if(currentTransceiver.getIp() != null || transceiver.getIp() != null){
            myHandler.post(updateRunnable);
            return true;
        }
        return false;
    }

    public abstract void updateFields();

    public static class RebootConfDialog extends DialogFragment {

        private OnTaskCompleted listener;

        public void setListener(OnTaskCompleted listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String rebootType = getArguments().getString(REBOOT_TYPE);
            String ip = getArguments().getString(BundleKeys.IP_KEY);
            String version = getArguments().getString(BundleKeys.VERSION_KEY);

            Logger.d(TAG, "show reboot/clear confirmation dialog: type - " + rebootType + ", ip: " + ip);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);

            if(rebootType.equals(REBOOT_RASP) || rebootType.equals(REBOOT_STM) || rebootType.equals(REBOOT_ALL)) {
                builder.setMessage(getString(R.string.confirm_reboot_of) + " " + rebootType);
            }
            else if(rebootType.equals(REBOOT_CLEAR)){
                builder.setMessage(R.string.ask_clear_the_transiver);
            }

            builder.setPositiveButton("ok", (dialog, id) -> {

                Logger.d(TAG, "on click, version: " + version + ", reboot type: " + rebootType);
                if(version != null) {

                    if (!version.equals("ssh_conn")) {
                        if (rebootType.equals(REBOOT_STM) || rebootType.equals(REBOOT_RASP) || rebootType.equals(REBOOT_ALL)) {
                            Thread thread = new Thread(new PostInfo(listener, ip,
                                    PostCommand.REBOOT + "_" + rebootType));
                            thread.start();
                        } else if (rebootType.equals(REBOOT_CLEAR)) {
                            Thread thread = new Thread(new PostInfo(listener, ip,
                                    PostCommand.ERASE_CONTENT));
                            thread.start();
                        }
                    } else {
                        SshConnection connection = new SshConnection(ip, listener);
                        switch (rebootType) {
                            case REBOOT_RASP:
                                connection.execute(SshConnection.REBOOT_CODE);
                                break;
                            case REBOOT_STM:
                                connection.execute(SshConnection.REBOOT_STM_CODE);
                                break;
                            case REBOOT_CLEAR:
                                connection.execute(SshConnection.CLEAR_RASP_CODE);
                                break;
                        }
                    }
                }
            });

            builder.setNegativeButton(getString(R.string.cancel), (dialog, id) -> {});

            builder.setCancelable(true);
            return builder.create();
        }
    }
}


class ContentClickListener implements View.OnClickListener{
//    private final Transceiver transiver;
    private final FragmentHandler fragmentHandler;
    private OnTaskCompleted listener;
    private String ip;
    private String version;
    public static final String TAG = ContentClickListener.class.getSimpleName();


    public void setListener(OnTaskCompleted listener) {
        this.listener = listener;
    }

    public ContentClickListener(Transceiver transiver,
                                FragmentHandler fragmentHandler){
//        this.transiver = transiver;
        this.fragmentHandler = fragmentHandler;
        this.ip = transiver.getIp();
        this.version = transiver.getVersion();
    }

    @Override
    public void onClick(View v) {

        Logger.d(TAG, "currentTransIp: " + ip + ", version: " + version);
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.IP_KEY, ip);
        bundle.putString(BundleKeys.VERSION_KEY, version);
        ContentFragment.RebootConfDialog dialog = new ContentFragment.RebootConfDialog();
        dialog.setListener(listener);


        switch (v.getId()) {
            case R.id.content_btn_rasp:
                Logger.d(TAG, "reboot raspberry btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_RASP);
                break;
            case R.id.content_btn_stm:
                Logger.d(TAG, "reboot stm btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_STM);
                break;
            case R.id.content_btn_clear:
                Logger.d(TAG, "clear btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_CLEAR);
                break;

            case R.id.content_btn_all:
                Logger.d(TAG, "reboot all btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_ALL);
                break;
        }

        dialog.setArguments(bundle);
        dialog.show(fragmentHandler.getFragmentManager(), FragmentHandler.CONFIRMATION_DIALOG_TAG);
    }
}
