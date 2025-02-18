package com.example.locationmonitoring.model;

public class StudentModelClass {

    String name,email,uid,gender,phone;

    public StudentModelClass(String name, String email, String uid, String gender,String phone) {
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.phone = phone;
        this.gender = gender;
    }
    public StudentModelClass() {
        // Default values or leave them as null
    }
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
