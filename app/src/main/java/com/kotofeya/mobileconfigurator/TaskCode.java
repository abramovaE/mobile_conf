package com.kotofeya.mobileconfigurator;

public interface TaskCode {
    int TAKE_CODE = 0;

    int UPDATE_OS_UPLOAD_CODE = 1;
    int UPDATE_STM_UPLOAD_CODE = 2;

    int REBOOT_CODE = 3;
    int REBOOT_STM_CODE = 4;
    int CLEAR_RASP_CODE = 5;
    int SEND_TRANSPORT_CONTENT_CODE = 6;
    int SEND_STATION_CONTENT_CODE = 7;

    int UPDATE_STM_DOWNLOAD_CODE = 8;
    int UPDATE_OS_VERSION_CODE = 9;
    int UPDATE_STM_VERSION_CODE = 10;
    int UPDATE_OS_DOWNLOAD_CODE = 11;

    int TRANSPORT_CONTENT_VERSION_CODE = 12;
    int STATION_CONTENT_VERSION_CODE = 13;

    int UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE = 16;
    int UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE = 17;

    int UPDATE_STATION_CONTENT_DOWNLOAD_CODE = 18;
    int UPDATE_STATION_CONTENT_UPLOAD_CODE = 19;

    int SSH_ERROR_CODE = 14;
    int DOWNLOADER_ERROR_CODE = 15;

    int DOWNLOAD_CITIES_CODE = 20;
    int SEND_LOG_TO_SERVER_CODE = 21;

    int UPDATE_CORE_UPLOAD_CODE = 22;
    int UPDATE_CORE_DOWNLOAD_CODE = 23;

    int UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE  = 24;
}