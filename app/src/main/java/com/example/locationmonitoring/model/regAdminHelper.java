package com.example.locationmonitoring.model;

import android.text.style.UnderlineSpan;

public class regAdminHelper {
    String name,username,password,email,Uid;


    public regAdminHelper(String Name, String Username, String Password, String Email, String UniqId) {
        this.name = Name;
        this.username = Username;
        this.password = Password;
        this.email = Email;
        this.Uid = UniqId;
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public  String getUid() { return Uid; }
    public  void  setUid(String Uniquid) { this.Uid = Uniquid; }

    public regAdminHelper() {
    }

}
