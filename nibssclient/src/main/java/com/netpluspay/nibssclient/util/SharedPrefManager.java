package com.netpluspay.nibssclient.util;

import android.text.TextUtils;

import com.dsofttech.dprefs.utils.DPrefs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netpluspay.nibssclient.models.User;
import com.netpluspay.nibssclient.models.UserData;

import java.lang.reflect.Type;

public class SharedPrefManager {

    private static int USER_TYPE_NONE = -1;
    private static final String TAG_TOKEN = "push_token";

    public static void setNetPlusPayConvenienceFee(Float netpluspayConvenienceFee) {
        DPrefs.INSTANCE.putFloat("netpluspay_convenience_fee", netpluspayConvenienceFee);
    }

    public static Float getNetPlusPayConvenienceFee() {
        return DPrefs.INSTANCE.getFloat("netpluspay_convenience_fee", 1.5f);
    }

    public static void setNextAgentTransactionsPage(int lastLoadedAgentTransactionsPage) {
        DPrefs.INSTANCE.putInt("LLATP", lastLoadedAgentTransactionsPage);
    }

    public static int getNextAgentTransactionsPage() {
        return DPrefs.INSTANCE.getInt("LLATP", 1);
    }

    public static void setLoginStatus(boolean status) {
        if (!status) {
            setUser(null);
        }
        DPrefs.INSTANCE.putBoolean("is_login", status);
    }

    public static boolean isLogin() {
        return DPrefs.INSTANCE.getBoolean("is_login", false);
    }

    public static void setAppToken(String appToken) {
        DPrefs.INSTANCE.putString("app_token", appToken);
    }

    public static String getAppToken() {
        return DPrefs.INSTANCE.getString("app_token", null);
    }

    public static void setAppTokenForNewStormService(String appToken) {
        DPrefs.INSTANCE.putString("app_token_for_new_storm_service", appToken);
    }

    public static String getAppTokenForNewStormService() {
        return DPrefs.INSTANCE.getString("app_token_for_new_storm_service", null);
    }

    public static String getLastLoggedInUser() {
        return DPrefs.INSTANCE.getString("last_logged_in_user", null);
    }

    public static void setLastLoggedInUser(String lastLoggedInUserId) {
        DPrefs.INSTANCE.putString("last_logged_in_user", lastLoggedInUserId);
    }

    public static void setUserType(int loginType) {
        DPrefs.INSTANCE.putInt("user_login_type", loginType);
    }

    public static int getUserType() {
        return DPrefs.INSTANCE.getInt("user_login_type", USER_TYPE_NONE);
    }

    public static void setXapiKey(String xapiKey) {
        DPrefs.INSTANCE.putString("service_x_api_key", xapiKey);
    }

    public static String getXapiKey() {
        return DPrefs.INSTANCE.getString("service_x_api_key", "");
    }

    public static Boolean hasAppToken() {
        return DPrefs.INSTANCE.getString("app_token", null) != null;
    }

    public static void setUserToken(String userToken) {
        DPrefs.INSTANCE.putString("user_token", userToken);
    }

    public static String getUserToken() {
        return DPrefs.INSTANCE.getString("user_token", null);
    }

    public static void setPOSConvenienceFee(Float convenience_fee) {
        DPrefs.INSTANCE.putFloat("pos_convenience_fee", convenience_fee);
    }

    public static Float getPOSConvenienceFee() {
        return DPrefs.INSTANCE.getFloat("pos_convenience_fee", 0.0f);
    }

    public static void setTransfeeConvenienceFee(Float convenience_fee) {
        DPrefs.INSTANCE.putFloat("transfer_convenience_fee", convenience_fee);
    }

    public static Float getTransfeeConvenienceFee() {
        return DPrefs.INSTANCE.getFloat("transfer_convenience_fee", 0.0f);
    }

    //this method will save the device token to shared preferences
    public static boolean saveDeviceToken(String token) {
        DPrefs.INSTANCE.putString(TAG_TOKEN, token);
        return true;
    }

    //this method will fetch the device token from shared preferences
    public static String getDeviceToken() {
        return DPrefs.INSTANCE.getString(TAG_TOKEN, null);
    }

    public static User getUser() {
        String userJSONString = DPrefs.INSTANCE.getString("user", "");
        if (TextUtils.isEmpty(userJSONString))
            return null;
        Type type = new TypeToken<User>() {
        }.getType();
        User user = new Gson().fromJson(userJSONString, type);
        return user;
    }

    public static void temp(String temp) {
        DPrefs.INSTANCE.putString("temp", temp);
    }

    public static String getTemp() {
        return DPrefs.INSTANCE.getString("temp", null);
    }

    public static void setUser(User user) {
        String userJSONString = new Gson().toJson(user);
        DPrefs.INSTANCE.putString("user", userJSONString);
    }

    public static void setUserData(UserData userData) {
        String userJSONString = new Gson().toJson(userData);
        DPrefs.INSTANCE.putString("userData", userJSONString);
    }

    public static UserData getUserData() {
        String userJSONString = DPrefs.INSTANCE.getString("userData", "");
        Type type = new TypeToken<UserData>() {
        }.getType();
        return (new Gson().fromJson(userJSONString, type));
    }

//    public static void setPartnerThreshold(Integer partnerThreshold) {
//        DPrefs.INSTANCE.putInt("userPartnerThreshold", partnerThreshold);
//    }
//
//    public static Integer getPartnerThreshold() {
//        return DPrefs.INSTANCE.getInt("userPartnerThreshold", 0);
//    }
}
