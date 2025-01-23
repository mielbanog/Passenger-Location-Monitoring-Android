package com.example.locationmonitoring.util;

import android.util.Log;

import com.example.locationmonitoring.model.RegistrationHelper;
import com.example.locationmonitoring.model.StudentModelClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PassengerDatalistfromStdCodeUtil {
    private static final String TAG = "PassengerDatalistfromStdCodeUtil";
    private static final String PASSENGER_PATH = "Passenger";

    public interface OnPassengerDataListFetchedListener {
        void onPassengerDataListFetched(List<StudentModelClass> passengerDataList);
    }

    public static DatabaseReference getPassengerRef() {
        return FirebaseDatabase.getInstance().getReference().child(PASSENGER_PATH);
    }

    public static void getPassengerDataListFromStdCodes(List<String> stdCodeList, final OnPassengerDataListFetchedListener listener) {
        List<StudentModelClass> passengerDataList = new ArrayList<>();

        for (String stdCode : stdCodeList) {
            getPassengerRef().orderByChild("uid").equalTo(stdCode)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot passengerSnapshot : dataSnapshot.getChildren()) {
                               // Log.d(TAG, "Student: " + passengerSnapshot);
                                StudentModelClass passengerData = new StudentModelClass();
                                passengerData.setUid(passengerSnapshot.child("uid").getValue(String.class));
                                passengerData.setEmail(passengerSnapshot.child("email").getValue(String.class));
                                passengerData.setName(passengerSnapshot.child("name").getValue(String.class));
                                passengerData.setGender( passengerSnapshot.child("gender").getValue(String.class));

                                passengerDataList.add(passengerData);
                            }

                            if (listener != null) {
                                listener.onPassengerDataListFetched(passengerDataList);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle error
                        }
                    });
        }
    }
}
