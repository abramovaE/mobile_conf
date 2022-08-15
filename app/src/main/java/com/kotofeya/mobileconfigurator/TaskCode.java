package com.kotofeya.mobileconfigurator;

public interface TaskCode {

    int REBOOT_CODE = 3;
    int REBOOT_STM_CODE = 4;
    int CLEAR_RASP_CODE = 5;
    int SEND_TRANSPORT_CONTENT_CODE = 6;
    int SEND_STATION_CONTENT_CODE = 7;

    int SSH_ERROR_CODE = 14;
    int DOWNLOADER_ERROR_CODE = 15;

    int DOWNLOAD_CITIES_CODE = 20;
    int SEND_LOG_TO_SERVER_CODE = 21;
}