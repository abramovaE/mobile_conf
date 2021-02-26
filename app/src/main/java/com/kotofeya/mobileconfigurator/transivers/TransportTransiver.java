package com.kotofeya.mobileconfigurator.transivers;

import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.City;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.MainActivity;

import java.io.UnsupportedEncodingException;

public class TransportTransiver extends Transiver {


    public TransportTransiver(ScanResult result) {
        super(result);
    }

    public TransportTransiver(String ssid, String ip, String macWifi, String macBt, String boardVersion, String osVersion,
                     String stmFirmware, String stmBootloader, String core, String modem, String incrementOfContent,
                     String uptime, String cpuTemp, String load, String tType) {
        super(ssid, ip, macWifi, macBt, boardVersion, osVersion,
                 stmFirmware, stmBootloader, core, modem, incrementOfContent,
                 uptime, cpuTemp, load, tType);
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


//        public String getFullNumber() {
//            StringBuilder sb = new StringBuilder();
//            sb.append("\"");
//            int number = getNumber();
//            if (getTransVersion() == VERSION_NEW) {
//                sb.append(getPreLitera());
//                sb.append(number);
//                sb.append(getPostLitera());
//            }
//            else {
//                sb.append(number);
//                sb.append(getPostLitera());
//            }
//            sb.append("\"");
//            return sb.toString();
//        }

    private String getFullNumnerVer0(){
        byte[] rawData = getRawData();
        StringBuilder sb = new StringBuilder();
        int number = ((rawData[17] & 0xff) << 8) + (rawData[18] & 0xff);
        String lit1 = getLitera(rawData[15]);
        String lit2 = getLitera(rawData[16]);
        String lit3 = getLitera(rawData[19]);
        String lit4 = getLitera(rawData[20]);

        String hard = "ъ";
        if((lit1 + lit2 + lit3 + lit4).contains(hard) || (lit1 + lit2 + lit3).contains(hard.toUpperCase())){
            return App.get().getResources().getString(R.string.transfer);
        }

        sb.append(lit1);
        sb.append(lit2);
        sb.append(number);
        sb.append(lit3);
        sb.append(lit4);
        if(number == 0xEEEE){
            return App.get().getResources().getString(R.string.withoutNumber);
        }
        return sb.toString();
    }

    private String getFullNumnerVer1(){
        byte[] rawData = getRawData();
        StringBuilder sb = new StringBuilder();
        int number = ((rawData[16] & 0xff) << 8) + (rawData[17] & 0xff);
        String lit1 = getLitera(rawData[15]);
        String lit2 = getLitera(rawData[18]);
        String lit3 = getLitera(rawData[19]);
        String hard = "ъ";
        if((lit1 + lit2 + lit3).contains(hard) || (lit1 + lit2 + lit3).contains(hard.toUpperCase())){
            return App.get().getResources().getString(R.string.transfer);
        }

        sb.append(lit1);
        sb.append(number);
        sb.append(lit2);
        sb.append(lit3);
        if(number == 0xEEEE){
            return App.get().getResources().getString(R.string.withoutNumber);
        }
        return sb.toString();
    }

    public String getFullNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        if (getTransVersion() == VERSION_NEW) {
            if(super.getBtPackVersion() == 0) {
                sb.append(getFullNumnerVer0());
            }
            else if(super.getBtPackVersion() > 0){
                sb.append(getFullNumnerVer1());
            }
        }
        else {
            byte[] rawData = getRawData();
            int number = ((rawData[3] & 0xff) << 8) + (rawData[4] & 0xff);
            sb.append(number);
            sb.append(getLitera(rawData[5]));
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

    private String getLitera(byte b){
        if((b & 0xff) != 0){
            try {
                return new String(new byte[]{b}, "Windows-1251").toUpperCase();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }


//    public String getLitera() {
//            byte[] rawData = getRawData();
//            StringBuilder sb = new StringBuilder();
//            if (getTransVersion() == VERSION_NEW) {
//                    sb.append(getLitera(rawData[15]));
//                    sb.append(getLitera(rawData[16]));
//                    sb.append(getLitera(rawData[19]));
//                    sb.append(getLitera(rawData[20]));
//            } else {
//                    sb.append(getLitera(rawData[15]));
//                    sb.append(getLitera(rawData[18]));
//                    sb.append(getLitera(rawData[19]));
//                }
//            return sb.toString();
//        }

    public String getLiteraN(int litNumber) {
        byte[] rawData = getRawData();
        StringBuilder sb = new StringBuilder();
        if (getBtPackVersion() >= 1) {
            switch (litNumber){
                case 1:
                    sb.append(getLitera(rawData[15]));
                    break;
                case 2:
                    sb.append(getLitera(rawData[18]));
                    break;
                case 3:
                    sb.append(getLitera(rawData[19]));
                    break;
            }
        } else {
            switch (litNumber){
                case 1:
                    sb.append(getLitera(rawData[15]));
                    break;
                case 2:
                    sb.append(getLitera(rawData[16]));
                    break;
                case 3:
                    sb.append(getLitera(rawData[19]));
                    break;
            }
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

    public String getStringDirection() {
        if(this.getNumber() != 0){
            return App.get().getResources().getStringArray(R.array.directions)[getIntDirection()];
        }
        return App.get().getResources().getStringArray(R.array.directions)[0];
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
            text.append("direction: " + this.getStringDirection());
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


    public String getCityCode(int cityId) {
            for(City c: MainActivity.cities){
                if(c.getId() == cityId){
                    return c.getIndex();
                }
            }
        return "";
    }
}

