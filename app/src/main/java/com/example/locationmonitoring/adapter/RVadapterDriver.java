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
import com.example.locationmonitoring.model.addDriverHelper;
import com.example.locationmonitoring.ui.Driverdetails;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RVadapterDriver extends RecyclerView.Adapter<RVVholder> {

    public static int driverPost;
    public static String driverID;
    private Context context;
    private List<addDriverHelper> addDriverHelperList;

    public RVadapterDriver(Context context, List<addDriverHelper> addDriverHelperList) {
        this.context = context;
        this.addDriverHelperList = addDriverHelperList;
    }

    @NonNull
    @Override
    public RVVholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driverlayout,parent,false);
        return new RVVholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVVholder holder, int position) {
        holder.name.setText(addDriverHelperList.get(position).getName());
        holder.plate.setText(addDriverHelperList.get(position).getPlate());
        holder.body.setText(addDriverHelperList.get(position).getBody());
        holder.qrCode.setText(addDriverHelperList.get(position).getQrCode());

        holder.details_Holder.setOnClickListener(new View.OnClickListener() {
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
        });
        holder.detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverPost =holder.getAdapterPosition();
                driverID = addDriverHelperList.get(holder.getAdapterPosition()).getQrCode();
                Log.d("Driver adapter: "," UID selected : "+ driverID);
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
                                try {

                                    String uid = addDriverHelperList.get(holder.getAdapterPosition()).getQrCode();
                                    DatabaseReference userLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers");

                                    // Reference to the specific UID node
                                    DatabaseReference uidRef = userLocationRef.child(uid);

                                    // Remove the node
                                    uidRef.removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                // Node deleted successfully
                                                // You can perform any additional actions here
                                            })
                                            .addOnFailureListener(e -> {
                                                // Failed to delete node
                                                // Handle the error
                                            });
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.Generate:
                                //TODO
                                if (!RVVholder.class.getName().equals("")){
                                    Intent intent = new Intent(view.getContext(), Driverdetails.class);
                                    intent.putExtra("name",addDriverHelperList.get(holder.getAdapterPosition()).getName());
                                    intent.putExtra("body",addDriverHelperList.get(holder.getAdapterPosition()).getBody());
                                    intent.putExtra("plate",addDriverHelperList.get(holder.getAdapterPosition()).getPlate());
                                    intent.putExtra("qrcode",addDriverHelperList.get(holder.getAdapterPosition()).getQrCode());

                                    view.getContext().startActivity(intent);
                                }else{
                                    Toast.makeText(view.getContext(),"Error",Toast.LENGTH_SHORT).show();
                                }
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
        return addDriverHelperList.size();
    }
}
class  RVVholder extends RecyclerView.ViewHolder{

    TextView name,plate,body,qrCode;
    RelativeLayout details_Holder;
    ImageButton detailsBtn;
    public RVVholder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.Driver_name);
        plate = itemView.findViewById(R.id.PlateDr);
        body = itemView.findViewById(R.id.BodyNo);
        qrCode = itemView.findViewById(R.id.qrCode);
        details_Holder = itemView.findViewById(R.id.Detail_Button_Holder);
        detailsBtn = itemView.findViewById(R.id.Detail_Button);
    }
}
