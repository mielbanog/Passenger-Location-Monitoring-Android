package com.example.locationmonitoring.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class RegistrationHelper  implements Parcelable {
    String name, birthday, gender, phone,email,qrCode,uid;

    public RegistrationHelper(String name, String birthday, String gender, String phone, String Email, String Qrcode, String UID) {
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.phone = phone;
        this.qrCode = Qrcode;
        this.email = Email;
        this.uid = UID;
    }
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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
    public  String getEmail(){return  email;}
    public  void  setEmail(String email) { this.email = email; }
    public String getQrCode(){return qrCode;}
    public void  setQrCode(String qrCode){
        this.qrCode = qrCode;
    }

    public RegistrationHelper() {
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + uid + '\'' +
                ", Name='" + name + '\'' +
                '}';
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(birthday);
        parcel.writeString(gender);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(qrCode);
        parcel.writeString(uid);
    }
}
