package com.example.locationmonitoring.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UserLocation implements Parcelable {

    private User user;
    private LocationData location;

    public UserLocation(User user, LocationData location) {
        this.user = user;
        this.location = location;
    }

    public UserLocation() {
    }

    //read data from model class
    protected UserLocation(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
        location = in.readParcelable(LocationData.class.getClassLoader());
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geo_point=" + location +
                '}';
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable((Parcelable) user, flags);

    }
}
