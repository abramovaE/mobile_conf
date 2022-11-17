package com.kotofeya.mobileconfigurator.network.request;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.SshUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

public class SshTakeInfoUseCase implements Runnable {

    private static final String TAG = SshTakeInfoUseCase.class.getSimpleName();
    private final SshTakeInfoListener listener;
    private final String ip;

    public SshTakeInfoUseCase(SshTakeInfoListener listener, String ip){
        Logger.d(TAG, "new SshTakeInfoConnectionRunnable(), ip: " + ip);
        this.listener = listener;
        this.ip = ip;
    }

    @Override
    public void run() {
            Session session = null;
            Channel channel = null;
            try {
                session = SshUtils.getSession(ip);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channel = session.openChannel("shell");
                OutputStream inputStream_for_the_channel = channel.getOutputStream();
                PrintStream commander = new PrintStream(inputStream_for_the_channel, true);
                channel.setOutputStream(baos, true);
                channel.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(App.get()
                        .getAssets().open("take.sh")));
                        String e;
                        while ((e = reader.readLine()) != null) {
                            commander.println(e);
                        }
                        do {
                            Thread.sleep(2000);
                        } while (!channel.isEOF());
                        reader.close();
                        commander.close();
                        String res = baos.toString()
                                .substring(baos.toString().lastIndexOf("$typeT") + 7,
                                        baos.toString().lastIndexOf("$ exit"));
                        listener.sshTakeInfoSuccessful(res);
            } catch (Exception e){
                listener.sshTakeInfoFailed(ip, e.getMessage());
            } finally {
                if(channel != null){
                    channel.disconnect();
                }
                if(session != null) {
                    session.disconnect();
                }
            }
    }
}