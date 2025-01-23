package com.example.locationmonitoring.util;


import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentStudntCodeUtils {
    private static final String TAG = "ParentStudentCodeUtils";
    private static final String PARENTS_PATH = "Parents";

    public interface OnStudentListFetchedListener {
        void onStudentListFetched(List<String> studentList);
    }

    public static DatabaseReference getParentsRef() {
        return FirebaseDatabase.getInstance().getReference().child(PARENTS_PATH);
    }

    public static void getStudentsFromParent(final FirebaseUser firebaseAuth, final OnStudentListFetchedListener listener) {
        if (firebaseAuth == null || firebaseAuth.getUid() == null) {
            // User is not authenticated
            // Handle this case, perhaps by redirecting to the login page
            return;
        }

        String uid = firebaseAuth.getUid();

        getParentsRef().child(uid).child("stdcode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> studentList = new ArrayList<>();


                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String studentValue = studentSnapshot.getValue(String.class);

                        if (studentValue != null) {
                            studentList.add(studentValue);
                        } else {
                            // Handle case when the student value is null
                            Log.d(TAG, "Error: Student value inside stdcode is null");
                        }
                    }
                }else {
                    String stdfirst = dataSnapshot.getValue(String.class);
                    studentList.add(stdfirst);
                }




                if (listener != null) {
                    listener.onStudentListFetched(studentList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Log.d(TAG, "Error: " + databaseError.getMessage());
            }
        });
    }
}
