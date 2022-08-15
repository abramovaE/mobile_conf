package com.kotofeya.mobileconfigurator;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.acra.ACRA;

//@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.KEY_VALUE_LIST)
//@AcraHttpSender(uri = "http://95.161.210.44/mobile_conf_acra.php",
//        httpMethod = HttpSender.Method.POST)
public class App extends Application {

    private static final String TAG = Application.class.getSimpleName();

    private static App instance;
    private static final String PREF_NAME = "mobile_conf_pref";

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String IS_REMEMBERED = "is_remembered";
    private static final String IS_ASK_FOR_TENETH = "is_ask_for_teneth";

    private String login;
    private String level;
    private SharedPreferences preferences;
    private boolean showAccessPointDialog;
    private String password;
    private boolean isRemembered;

    public static App get() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            Logger.d(Logger.MAIN_LOG, "class for name: " + Class.forName("com.jcraft.jsch.jce.Random"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        showAccessPointDialog = preferences.getBoolean(IS_ASK_FOR_TENETH, true);
        login = preferences.getString(LOGIN, "");
        password = preferences.getString(PASSWORD, "");
        isRemembered = preferences.getBoolean(IS_REMEMBERED, false);
    }

    public boolean isRemembered() {
        return isRemembered;
    }

    public String getPassword() {
        return password;
    }


    public void saveLoginInformation(String login, String password, boolean isRemembered){
        if(isRemembered){
            preferences.edit().putString(LOGIN, login).apply();
            preferences.edit().putString(PASSWORD, password).apply();
            preferences.edit().putBoolean(IS_REMEMBERED, true).apply();
        } else {
            resetLoginInformation();
        }
    }

    public void resetLoginInformation(){
        preferences.edit().putString(LOGIN, "").apply();
        preferences.edit().putString(PASSWORD, "").apply();
        preferences.edit().putBoolean(IS_REMEMBERED, false).apply();
    }

    public boolean isAskForTeneth(){
        return preferences.getBoolean(IS_ASK_FOR_TENETH, true);
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public void setAskForTeneth(boolean value) {
        preferences.edit().putBoolean(IS_ASK_FOR_TENETH, value).apply();
        this.showAccessPointDialog = value;
    }

    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
}