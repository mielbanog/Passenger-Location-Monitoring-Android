package com.example.locationmonitoring.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String email;
    private String uid;
    private String name;
    private String CurrentDriver_Id;

    public User(String email, String UID, String name) {
        this.email = email;
        this.uid = UID;
        this.name = name;
    }

    public User(String email, String uid, String name, String currentDriver_Id) {
        this.email = email;
        this.uid = uid;
        this.name = name;
        CurrentDriver_Id = currentDriver_Id;
    }

    public User() {

    }
    protected User(Parcel in) {
        email = in.readString();
        uid = in.readString();
        name = in.readString();
        CurrentDriver_Id = in.readString();

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

    public String getCurrentDriver_Id() {
        return CurrentDriver_Id;
    }

    public void setCurrentDriver_Id(String currentDriver_Id) {
        CurrentDriver_Id = currentDriver_Id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + uid + '\'' +
                ", username='" + name + '\'' +
                ", currentDriver='" + CurrentDriver_Id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(CurrentDriver_Id);
    }
}
