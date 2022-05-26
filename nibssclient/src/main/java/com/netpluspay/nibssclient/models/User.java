package com.netpluspay.nibssclient.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "users")
/*,indices = {@Index(value = {"email"},
        unique = true)}*/
public class User {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    //@SerializedName(value="user_id")
    @SerializedName(value = "agent_id", alternate = {"Merchant_id", "merchant_id"})
    @Expose
    private Long user_id;

    @SerializedName(value = "user_type")
    @Expose
    private Integer user_type;

    @SerializedName(value = "role")
    @Expose
    private String user_type_for_new_storm_service;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("mobile_no")
    @Expose
    private String phone;

    @SerializedName("account_no")
    @Expose
    private String account_number;

    @SerializedName("bank")
    @Expose
    private String bank;

    @SerializedName("bvn_no")
    @Expose
    private String bvn;

    @SerializedName("terminal_id")
    @Expose
    private String terminal_id;

    @SerializedName("is_verified")
    @Expose
    private Boolean is_verified;

    @SerializedName(value = "amount", alternate = {"wallet_amount","availableBalance"})
    @Expose
    private String availableBalance = "0.00";

    @SerializedName(value = "ledgerBalance")
    @Expose
    private String ledgerBalance = "0.00";

    public Long getUser_id() {
        return user_id;
    }

    @SerializedName("isQRRegistered")
    @Expose
    private Boolean isQRRegistered = false;

    @SerializedName("state")
    @Expose
    private String state = "";


    @SerializedName("netplus_id")
    @Expose
    private String netplus_id;

    @SerializedName("business_name")
    @Expose
    private String business_name;


    @SerializedName("business_address")
    @Expose
    private String business_address;

    @SerializedName("business_state")
    @Expose
    private String business_state;

    @SerializedName("business_phone_number")
    @Expose
    private String business_phone_number;

    @Ignore
    @SerializedName("business_info")
    @Expose
    private Business business_info;

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Integer getUser_type() {
        return user_type;
    }

    public void setUser_type(Integer user_type) {
        this.user_type = user_type;
    }



    public String getUser_type_for_new_storm_service() {
        return user_type_for_new_storm_service;
    }

    public void setUser_type_for_new_storm_service(String user_type_for_new_storm_service) {
        this.user_type_for_new_storm_service = user_type_for_new_storm_service;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIs_verified() {
        return is_verified;
    }

    public void setIs_verified(Boolean is_verified) {
        this.is_verified = is_verified;
    }

    public String getAvailableBalance() {
        return availableBalance == null ? "0.00" : availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Boolean getQRRegistered() {
        return isQRRegistered;
    }

    public void setQRRegistered(Boolean QRRegistered) {
        isQRRegistered = QRRegistered;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNetplus_id() {
        return netplus_id;
    }

    public void setNetplus_id(String netplus_id) {
        this.netplus_id = netplus_id;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getBusiness_address() {
        return business_address;
    }

    public void setBusiness_address(String business_address) {
        this.business_address = business_address;
    }

    public String getBusiness_state() {
        return business_state;
    }

    public void setBusiness_state(String business_state) {
        this.business_state = business_state;
    }

    public String getBusiness_phone_number() {
        return business_phone_number;
    }

    public void setBusiness_phone_number(String business_phone_number) {
        this.business_phone_number = business_phone_number;
    }

    public Business getBusiness_info() {
        return business_info;
    }

    public void setBusiness_info(Business business_info) {
        this.business_info = business_info;
    }

    public String getTerminal_id() {
        return terminal_id;
    }

    public void setTerminal_id(String terminal_id) {
        this.terminal_id = terminal_id;
    }

    public String getLedgerBalance() {
        return ledgerBalance;
    }

    public void setLedgerBalance(String ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
    }
}
