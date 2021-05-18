package com.kotofeya.mobileconfigurator.transivers;


public class TriolInformer extends StatTransiver {

    private int prevCounterStatus;
    private long prevCounterTime;
    private String addInfo;
    private boolean isWorking;

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean isWorking){
        this.isWorking = isWorking;
    }

    @Override
    public void setRawData(byte[] rawData) {
        super.setRawData(rawData);
        prevCounterStatus = (getRawData()[7]  >> 4) & 0b00001111;
        prevCounterTime = System.currentTimeMillis()/1000;
    }

    public int getCounterStatus(){
        return (getRawData()[7]  >> 4) & 0b00001111;
    }

    public int getCounterCall(){
        return getRawData()[7] & 0b00001111;
    }

    public long getPrevCounterTime() {
        return prevCounterTime;
    }

    public int getMode(){
        return getRawData()[6] & 0xff;
    }


    public long getTime(){
        return (((getRawData()[8] & 0xFF) << 24) + ((getRawData()[9] & 0xFF) << 16)
                + ((getRawData()[10] & 0xFF) << 8) + ((getRawData()[11] & 0xFF)));
    }

    public int getStreetId(){
        return ((getRawData()[12] & 0xFF) << 8) + ((getRawData()[13] & 0xFF));
    }

    public int getStreetSide(){
        return getRawData()[16] & 0xFF;
    }

    public int getCityIndex() {
        return 0;
    }

    @Override
    public int getType() {
        return 0;
    }

    public int getImageId() {
        return 0;
    }

    public String getAddInfo() {
        return addInfo;
    }

    public void setAddInfo(String s) {
        this.addInfo = s;
    }

    public TriolInformer(int rssi, String address, String deviceName, byte[] data) {
        super(rssi, address, deviceName, data);
        this.addInfo = "";
    }

    public String getDevInfo() {
        StringBuilder res = new StringBuilder();
//        res.append(getSsid() + " version: " + getStringVersion());
        res.append("\n");
        res.append("inf state: ready/busy/called");
        res.append("\n");
        for(int i = 0; i < 4; i++){
            res.append("inf" + i + ": " +  isCallReady(i) + "/" + isCallBusy(i) + "/" + isCalled(i));
            res.append("\n");
        }
        res.append("serial: " + getSsid());
        res.append("\n");
        res.append("mode: " + getMode());
        res.append("\n");
        res.append("counter status: " + getCounterStatus());
        res.append("\n");
        res.append("counter call: " + getCounterCall());
        res.append("\n");
        res.append("time: " + getTime());
        res.append("\n");
        res.append("street id: " + getStreetId());
        res.append("\n");
        res.append("street side: " + getStreetSide());
        res.append("\n");
        return res.toString();
    }
}