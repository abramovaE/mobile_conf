package com.kotofeya.mobileconfigurator.network;

public interface PostCommand {
//    ver - версия php скрипта
    String VERSION = "ver";
//    takeInfoFull - запрос JSON массива информации
    String TAKE_INFO_FULL = "takeInfoFull";
//    stm_update_log - листинг лог файла обновления STM
    String STM_UPDATE_LOG = "stm_update_log";
//    stm_update_log_clear - очистка лог файла обновления STM (приведет к повторной загрузке имеющегося на малине контента в STM)
    String STM_UPDATE_LOG_CLEAR = "stm_update_log_clear";
//    erase_content - удаление контента (очистка директорий data, рекурсивно)
    String ERASE_CONTENT = "erase_content";
    String TRANSP_CONTENT = "transp";
    String REBOOT = "reboot";
    String READ_WPA = "read_wpa";
    String READ_NETWORK = "read_network";


    String WIFI_CLEAR = "wifi_clear";

    //    floor - установить этаж трансивера (пример: floor_1 - установит первый этаж)
    default String floor(int floorNumber){
        return "floor_" + floorNumber;
    }
//    sound - установить тип звучания маяка (1 - комнатный, 2 - уличный). Пример: sound_2 - установит тип звучания "уличный"
//    Иные значения не примутся системой
    default String sound(int soundNumber){
        return "sound_" + soundNumber;
    }
//    volume - установить громкость звукового маяка (0-100). Пример: volume_20 - установит громкость на 20 (зависимость не линейная)
//    Иные значения не примутся системой
    default String volume(int volumeValue){
        return "volume_" + volumeValue;
    }
//    reboot - инициирует перезапуск система
//    rasp - перезапуск raspberry
//    stm - перезапуск контроллера
//    all - перезапуск обоих в порядке, сначала STM потом Raspberry
    default String reboot(String device){
        return "reboot_" + device;
    }
//    transp - конфигурация транспортного трансивера
//    TYPETRANS_LIT_NUM_LIT_LIT_NAPR - задается в виде аналогичном читаемому
//    пример: transp_3_а_5_б_в_1 - задаст конфигурацию "Троллейбус А5БВ прямо"
//    TYPETRANS - числовое представление типа транспорта
//    LIT - литера
//    NUM - номер маршрута
//    LIT - литера
//    LIT - литера
//    NAPR - направление (1 - прямо, 2 - обратно).
    default String transpConfig(String type, String lit1, String num, String lit2, String lit3, String dir){
//        if(lit1 != null && !lit1.isEmpty()){
            lit1 = lit1 + "_";
//        }
//        if(lit2 != null && !lit2.isEmpty()){
            lit2 = lit2 + "_";
//        }
//        if(lit3 != null && !lit3.isEmpty()){
            lit3 = lit3 + "_";
//        }
        return "transp_" + type + "_" + lit1 + num + "_" + lit2 + lit3 + dir;
    }
//    static - настройка статического IP ethernet интерфейса
//    clear - привести конфигурацию к стандартному виду (настройки примутся при перезапуске, он инициируется отдельно)
//    пример: static_clear - дефолтные настройки
//    IP_GATE_MASK - задаем параметры статики ethernet интерфейса
//    пример: static_10.43.44.15_10.43.44.1_255.255.255.254 - задаст:
//    IP - 10.43.44.15
//    GATE - 10.43.44.1
//    MASK - 255.255.255.254
//    (настройки примутся при перезапуске, он инициируется отдельно)
    default String staticEthernet(String... args){
        StringBuilder staticCommand = new StringBuilder("static");
        for(String s:args){
            staticCommand.append("_" + s);
        }
        return staticCommand.toString();
    }
//    wifi - настройка WIFI интерфейса
//    clear - привести конфигурацию к стандартному виду (настройки примутся при перезапуске, он инициируется отдельно)
//    Пример: wifi_clear - дефолтные настройки
//    SSID_PASSW - задаем пареметры дополнительной WIFI сети
//    пример: wifi_newSSiD_verYcooLpass - задаст:
//    SSID - newSSiD
//    PASSW - verYcooLpass
//            (настройки примутся при перезапуске, он инициируется отдельно)
//    Имеются ограничения для пароля - от 8 до 63 символов, система сообщит если правило нарушено
    String WIFI = "wifi";
    default String wifi(String... args){
        StringBuilder wifiCommand = new StringBuilder("wifi");
        for(String s:args){
            wifiCommand.append("_" + s);
        }
        return wifiCommand.toString();
    }

    String POST_COMMAND_ERROR = "postCommandError";
}
