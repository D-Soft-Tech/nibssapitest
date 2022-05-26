package com.netpluspay.nibssclient.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netpluspay.nibssclient.models.User;
import com.pixplicity.easyprefs.library.Prefs;

import java.lang.reflect.Type;

public class SharedPrefManager2 {

    private static int USER_TYPE_NONE= -1;
    private static final String TAG_TOKEN = "push_token";

    public static void setNetPlusPayConvenienceFee(Float netpluspayConvenienceFee) {
        Prefs.putFloat("netpluspay_convenience_fee", netpluspayConvenienceFee);
    }

    public static Float getNetPlusPayConvenienceFee() {
        return Prefs.getFloat("netpluspay_convenience_fee", 1.5f);
    }

    public static void setNextAgentTransactionsPage(int lastLoadedAgentTransactionsPage) {
        Prefs.putInt("LLATP", lastLoadedAgentTransactionsPage);
    }

    public static int getNextAgentTransactionsPage() {
        return Prefs.getInt("LLATP", 1);
    }

    public static void setLoginStatus(boolean status) {
        if (!status) {
            setUser(null);
        }
        Prefs.putBoolean("is_login", status);
    }

    public static boolean isLogin() {
        return Prefs.getBoolean("is_login", false);
    }

    public static void setAppToken(String appToken) {
        Prefs.putString("app_token", appToken);
    }

    public static String getAppToken() {
        return Prefs.getString("app_token", null);
    }

    public static void setAppTokenForNewStormService(String appToken) {
        Prefs.putString("app_token_for_new_storm_service", appToken);
    }

    public static String getAppTokenForNewStormService() {
        return Prefs.getString("app_token_for_new_storm_service", null);
    }

    public static String getLastLoggedInUser() {
        return Prefs.getString("last_logged_in_user", null);
    }

    public static void setLastLoggedInUser(String lastLoggedInUserId) {
        Prefs.putString("last_logged_in_user", lastLoggedInUserId);
    }

    public static void setUserType(int loginType) {
        Prefs.putInt("user_login_type", loginType);
    }

    public static int getUserType() {
        return Prefs.getInt("user_login_type", USER_TYPE_NONE);
    }

    public static void setXapiKey(String xapiKey) {
        Prefs.putString("service_x_api_key", xapiKey);
    }

    public static String getXapiKey() {
        return Prefs.getString("service_x_api_key", "");
    }

    public static Boolean hasAppToken() {
        return Prefs.getString("app_token", null) != null;
    }

    public static void setUserToken(String userToken) {
        Prefs.putString("user_token", userToken);
    }

    public static String getUserToken() {
        return Prefs.getString("user_token", null);
    }

    public static void setPOSConvenienceFee(Float convenience_fee) {
        Prefs.putFloat("pos_convenience_fee", convenience_fee);
    }

    public static Float getPOSConvenienceFee() {
        return Prefs.getFloat("pos_convenience_fee", 0.0f);
    }

    public static void setTransfeeConvenienceFee(Float convenience_fee) {
        Prefs.putFloat("transfer_convenience_fee", convenience_fee);
    }

    public static Float getTransfeeConvenienceFee() {
        return Prefs.getFloat("transfer_convenience_fee", 0.0f);
    }

    //this method will save the device token to shared preferences
    public static boolean saveDeviceToken(String token) {
        Prefs.putString(TAG_TOKEN, token);
        return true;
    }

    //this method will fetch the device token from shared preferences
    public static String getDeviceToken() {
        return Prefs.getString(TAG_TOKEN, null);
    }


    public static User getUser() {
        String userJSONString = Prefs.getString("user", "");
        if (TextUtils.isEmpty(userJSONString))
            return null;
        Type type = new TypeToken<User>() {
        }.getType();
        User user = new Gson().fromJson(userJSONString, type);
        return user;
    }

    public static void temp(String temp) {
        Prefs.putString("temp", temp);
    }

    public static String getTemp() {
        return Prefs.getString("temp", null);
    }

    public static void setUser(User user) {
        String userJSONString = new Gson().toJson(user);
        Prefs.putString("user", userJSONString);
    }
}
