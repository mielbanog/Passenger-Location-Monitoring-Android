package com.example.locationmonitoring.model;

public class regParentsHelper {

    String parentName,parentUser,parentPass,parentEmail,parentPhone,parentUniqId,stdcode,uid;

    public regParentsHelper(String Name, String Password, String Email, String Phone, String UniqId,String StdCode,String Uid) {
        this.parentName = Name;
        this.parentPass = Password;
        this.parentEmail = Email;
        this.parentPhone = Phone;
        this.parentUniqId = UniqId;
        this.stdcode = StdCode;
        this.uid = Uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentUser() {
        return parentUser;
    }

    public void setParentUser(String parentUser) {
        this.parentUser = parentUser;
    }

    public String getParentPass() {
        return parentPass;
    }

    public void setParentPass(String parentPass) {
        this.parentPass = parentPass;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getParentUniqId() {
        return parentUniqId;
    }

    public void setParentUniqId(String parentUniqId) {
        this.parentUniqId = parentUniqId;
    }

    public String getStdcode() {
        return stdcode;
    }

    public void setStdcode(String stdcode) {
        this.stdcode = stdcode;
    }
}
