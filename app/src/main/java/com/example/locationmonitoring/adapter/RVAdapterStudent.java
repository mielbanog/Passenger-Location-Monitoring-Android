package com.example.locationmonitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.StudentModelClass;
import com.example.locationmonitoring.model.User;
import com.example.locationmonitoring.model.addDriverHelper;
import com.example.locationmonitoring.ui.Driverdetails;
import com.example.locationmonitoring.ui.LocationMonitoring;
import com.example.locationmonitoring.ui.Parents;
import com.example.locationmonitoring.ui.ParentsPage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RVAdapterStudent extends RecyclerView.Adapter<VwHolder> {

    private static final String TAG = "RVadapterStudent";
    public static int driverPost;
    public  String driverID;
    private Context context;
    private List<StudentModelClass> userList;

    public RVAdapterStudent(Context context, List<StudentModelClass> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public VwHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driverlayout,parent,false);
        return new VwHolder(view);
    }

    //
    @Override
    public void onBindViewHolder(@NonNull VwHolder holder, int position) {

        holder.name.setText(userList.get(position).getName());
        holder.email.setText(userList.get(position).getEmail());
        holder.uid.setText(userList.get(position).getUid());

       /* holder.details_Holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverPost =holder.getAdapterPosition();
                driverID = holder.qrCode.getText().toString();

                PopupMenu popupMenu = new PopupMenu(holder.detailsBtn.getContext(), holder.detailsBtn);
                popupMenu.getMenuInflater().inflate(R.menu.pop_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.Update:
                                //TODO
                                break;
                            case R.id.Delete:
                                //TODO
                                break;
                            case R.id.Generate:
                                //TODO
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });*/
        holder.detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverPost =holder.getAdapterPosition();
                driverID = userList.get(position).getUid();
                Toast.makeText(view.getContext(),"UID: "+ driverID,Toast.LENGTH_SHORT).show();
                //Log.d(TAG,"UID: Successfully retrieve uid"+driverID);
                PopupMenu popupMenu = new PopupMenu(holder.detailsBtn.getContext(), holder.detailsBtn);
                popupMenu.getMenuInflater().inflate(R.menu.studentviewpopup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.viewRoute:
                                //TODO
                                if (!RVVholder.class.getName().equals("") && !(driverID == null)){
                                    Intent intent = new Intent(view.getContext(), LocationMonitoring.class);
                                    intent.putExtra("name",userList.get(holder.getAdapterPosition()).getName());
                                    intent.putExtra("email",userList.get(holder.getAdapterPosition()).getEmail());
                                    intent.putExtra("uid",driverID);

                                    view.getContext().startActivity(intent);
                                }else{
                                    Toast.makeText(view.getContext(),"Error",Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case R.id.deleteStudent:
                                //TODO

                                // Get reference to the Parents table in Firebase
                                DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference().child("Parents");

                                parentsRef.child("stdcode").child(driverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                            // Remove the node with the matching stdcode
                                            childSnapshot.getRef().removeValue()
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Node deleted successfully
                                                        // You can perform any additional actions here
                                                        Log.d(TAG, "Delete: Successfully removed User with stdcode: " + driverID);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Failed to delete node
                                                        // Handle the error
                                                        Log.d(TAG, "Delete: Failed to remove User with stdcode: " + driverID);
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Handle the error
                                        Log.d(TAG, "Delete: DatabaseError - " + databaseError.getMessage());
                                    }
                                });
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
class VwHolder extends RecyclerView.ViewHolder{

    TextView name,email,uid;
    RelativeLayout details_Holder;
    ImageButton detailsBtn;
    public VwHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.Driver_name);
        email = itemView.findViewById(R.id.PlateDr);
        uid = itemView.findViewById(R.id.qrCode);
        details_Holder = itemView.findViewById(R.id.Detail_Button_Holder);
        detailsBtn = itemView.findViewById(R.id.Detail_Button);
    }

}
