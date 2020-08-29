package com.kotofeya.mobileconfigurator.transivers;

import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;

import java.io.UnsupportedEncodingException;

public class TransportTransiver extends Transiver {


    public TransportTransiver(ScanResult result) {
        super(result);
    }

    public TransportTransiver(String ssid, String ip, String macWifi, String macBt, String boardVersion, String osVersion,
                     String stmFirmware, String stmBootloader, String core, String modem, String incrementOfContent,
                     String uptime, String cpuTemp, String load) {
        super(ssid, ip, macWifi, macBt, boardVersion, osVersion,
                 stmFirmware, stmBootloader, core, modem, incrementOfContent,
                 uptime, cpuTemp, load);
    }


        public static final int PARK = 0;
        public static final int DIRECT = 1;
        public static final int REVERSE = 2;


        public int getCity(){
            if(getTransVersion() == VERSION_NEW){
                return (((getRawData()[12] & 0xff)) + (getRawData()[13] & 0xff));
            }

            return 0;
        }

        public int getTransportType(){
            if(getTransVersion() == VERSION_NEW){
                return getRawData()[14] & 0xff;
            }
            else {
                return (getRawData()[2] & 0xff);
            }
        }



        public String getFullNumber() {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            int number = getNumber();
            if (getTransVersion() == VERSION_NEW) {
                sb.append(getPreLitera());
                sb.append(number);
                sb.append(getPostLitera());
            }
            else {
                sb.append(number);
                sb.append(getPostLitera());
            }
            sb.append("\"");
            return sb.toString();
        }

        public int getNumber(){
            byte[] rawData = getRawData();
            String number = "";
            if (getTransVersion() == VERSION_NEW) {
                number = ((rawData[17] & 0xff) << 8) + (rawData[18] & 0xff) + "";
            }
            else {
                number = ((rawData[3] & 0xff) << 8) + (rawData[4] & 0xff) + "";
            }
            Logger.d(Logger.TRANSPORT_CONTENT_LOG, "number: " + number);
            return Integer.parseInt(number);

        }

        public String getPreLitera() {
            byte[] rawData = getRawData();
            StringBuilder sb = new StringBuilder();
            try {
                if (getTransVersion() == VERSION_NEW) {
                    if ((rawData[15] & 0xff) != 0) {
                        byte tmpArr15[] = {rawData[15]};
                            sb.append(new String(tmpArr15, "Windows-1251").toUpperCase());
                    }
                    if ((rawData[16] & 0xff) != 0) {
                        byte tmpArr16[] = {rawData[16]};
                        sb.append(new String(tmpArr16, "Windows-1251").toUpperCase());
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        public String getPostLitera(){
            byte[] rawData = getRawData();
            StringBuilder sb = new StringBuilder();
            try {
                if (getTransVersion() == VERSION_NEW) {
                    if ((rawData[19] & 0xff) != 0) {
                        byte tmpArr19[] = {rawData[19]};
                        sb.append(new String(tmpArr19, "Windows-1251").toUpperCase());
                    }
                    if ((rawData[20] & 0xff) != 0) {
                        byte tmpArr20[] = {rawData[20]};
                        sb.append(new String(tmpArr20, "Windows-1251").toUpperCase());
                    }
                } else {
                    if ((rawData[5] & 0xff) != 0) {
                        byte tmpArr[] = {rawData[5]};
                        sb.append(new String(tmpArr, "Windows-1251").toUpperCase());
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        public int getDirection() {
            if(this.getNumber() == 0){
                return PARK;
            }

            else {
                int dir = getIntDirection();
                switch (dir){
                    case 0:
                        return PARK;
                    case 1:
                        return DIRECT;
                    case 2:
                        return  REVERSE;
                }
                return 0;
            }
        }

        public int getIntDirection(){
            int dir;
            if(getTransVersion() == VERSION_NEW){
                dir = (getRawData()[7] >> 4) & 0b00000011;
            }
            else {
                dir = getRawData()[6] & 0xff;
            }
            return dir;
        }

        private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

        private String byteToHex(byte bytes) {
            char[] hexChar = new char[2];
            int v = bytes & 0xff;
            hexChar[0] = hexArray[v >>> 4];
            hexChar[1] = hexArray[v & 0x0F];
            return new String(hexChar);
        }

    @Override
    String getFirstPartExpInfo() {
            StringBuilder text = new StringBuilder();
            text.append("doors status: " + getStringDoorStatus(this.getFloorOrDoorStatus()));
            text.append("\n");
            text.append("direction: " + this.getDirection());
            text.append("\n");
            text.append("city: " +  this.getCity());
            text.append("\n");
            return text.toString();
    }

    @Override
    String getSecondPartExpInfo() {
        StringBuilder text = new StringBuilder();
        text.append(getTransportType());
        text.append(" ");
        text.append(getFullNumber());
        return text.toString();
    }
}

