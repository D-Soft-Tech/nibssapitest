package com.netpluspay.nibssclient.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "business_info")
public class Business {

    @PrimaryKey//(autoGenerate = true)
    @NonNull
    @SerializedName(value="user_id")
    @Expose
    private Long user_id;


    @SerializedName("business_name")
    @Expose
    private String business_name;


    @SerializedName("business_address")
    @Expose
    private String business_address;

    @SerializedName("business_state")
    @Expose
    private String business_state;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("phone")
    @Expose
    private String phone;


    public Long getUser_id() {
        return user_id;
    }


    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getBusiness_address() {
        return business_address;
    }

    public void setBusiness_address(String business_address) {
        this.business_address = business_address;
    }

    public String getBusiness_name() {
        return TextUtils.isEmpty(business_name)?"":business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getBusiness_state() {
        return business_state;
    }

    public void setBusiness_state(String business_state) {
        this.business_state = business_state;
    }
}