package com.kotofeya.mobileconfigurator.network.request;

import com.jcraft.jsch.Session;
import com.kotofeya.mobileconfigurator.network.SshUtils;

public class SendSshCommandUseCase implements Runnable {

    private static final String TAG = SendSshCommandUseCase.class.getSimpleName();
    private String ip;
    private SendSshCommandListener listener;
    private String command;

    public SendSshCommandUseCase(String ip,
                                 SendSshCommandListener listener,
                                 String command) {
        this.ip = ip;
        this.listener = listener;
        this.command = command;
    }

    @Override
    public void run() {
        Session session = null;
        try {
            session = SshUtils.getSession(ip);
            String res = SshUtils.execCommand(session, command);
            listener.sendSshCommandSuccessful(res);
        } catch (Exception e){
            listener.sendSshCommandFailed(e.getMessage());
        } finally {
            if(session != null) {
                session.disconnect();
            }
        }
    }
}