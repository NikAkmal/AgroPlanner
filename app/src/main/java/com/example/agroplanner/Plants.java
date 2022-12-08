package com.example.agroplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class Plants extends AppCompatActivity {

    //Tap to add button
    Button button;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Crops, CustomAdapter> recyclerAdapter;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    Dialog dialog;
    private CardView btn_feed, btn_report;
    private Button delete, edit, edit_plant;
    AlertDialog.Builder builder;
    EditText editText;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants);
        builder = new AlertDialog.Builder(this);
        editText = new EditText(this);

        //Firebase init
        database = FirebaseDatabase.getInstance();
        id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = database.getReference().child("Plants/"+id);
//        databaseReference = (DatabaseReference) database.getReference("Plants").orderByChild("id").equalTo(id);
        dialog = new Dialog(this);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);

        loadData();

        //Button for adding plot
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlot1();
            }
        });
    }

    private void loadData() {
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Crops>()
                        .setQuery(databaseReference,Crops.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Crops, CustomAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CustomAdapter holder, int position, @NonNull Crops model) {

                String imageUrl = model.getImageUrl();
                Glide.with(getApplicationContext()).load(model.getImageUrl())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(holder.plantpicture);

                holder.plant.setText("Plant Name: " +model.getPlants());
                holder.plot.setText("Plot Name: " +model.getPlot());

                String plantName = model.getPlants();
                String plotName = model.getPlot();
                String plotid = model.getPlotid();

                holder.plant.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openDialog(plantName, plotName, plotid, imageUrl);
                    }
                });

                holder.plot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openDialog(plantName, plotName, plotid, imageUrl);
                    }
                });

                holder.plantpicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openDialog(plantName, plotName, plotid, imageUrl);
                    }
                });
        }

        @NonNull
        @Override
        public CustomAdapter onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.plants2,viewGroup,false);
        return new CustomAdapter(view);
        }};

        recyclerAdapter.notifyDataSetChanged();
        recyclerAdapter.startListening();
        recyclerView.setAdapter(recyclerAdapter);
    }

    public void openDialog(String plantName, String plotName, String plotid, String imageUrl){

        dialog.setContentView(R.layout.activity_plant_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btn_feed = dialog.findViewById(R.id.btn_feed);
        btn_report = dialog.findViewById(R.id.btn_report);
        delete = dialog.findViewById(R.id.delete);
        edit = dialog.findViewById(R.id.edit);
        edit_plant = dialog.findViewById(R.id.edit_plant);
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btn_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new  Intent(getApplicationContext(), PostImage.class);
                Bundle extras = new Bundle();
                extras.putString("plantName", plantName);
                extras.putString("plotName", plotName);
                extras.putString("plotid", plotid);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new  Intent(getApplicationContext(), CalendarActivity.class);
                Bundle extras = new Bundle();
                extras.putString("plantName", plantName);
                extras.putString("plotName", plotName);
                extras.putString("plotid", plotid);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                builder.setCancelable(true);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this Crop?");
                builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Remove Crops
                            FirebaseDatabase.getInstance().getReference("Plants/"+id+"/"+plotid)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        //Remove Record
                                        FirebaseDatabase.getInstance().getReference("Record/"+plotid)
                                        .removeValue();

                                        //Remove Crops Picture
                                        StorageReference photoDelete1 = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                        photoDelete1.delete();
                                        Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();

                                        //Delete Image in Storage Code
                                        StorageReference photoDelete2 = FirebaseStorage.getInstance().getReference().child("Plants/"+id+"/"+plotid+"/Daily");
                                        photoDelete2.listAll()
                                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                            @Override
                                            public void onSuccess(ListResult listResult) {
//                                                    for (StorageReference prefix : listResult.getPrefixes()) {
//                                                        // All the prefixes under listRef.
//                                                        // You may call listAll() recursively on them.
//                                                    }

                                                for (StorageReference item : listResult.getItems()) {
                                                    // All the items under listRef.
                                                    item.delete();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Uh-oh, an error occurred!
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                builder.setTitle("Edit Plot");

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
                editText.setLayoutParams(lp);
                builder.setView(editText);

                getText(plotid, editText);

                builder.setPositiveButton("Edit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("plot", editText.getText().toString());

                                FirebaseDatabase.getInstance().getReference("Plants/"+id)
                                        .child(plotid).updateChildren(hashMap);
                                Intent intent = new  Intent(getApplicationContext(), Plants.class);
                                startActivity(intent);
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
            }
        });

        edit_plant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                builder.show().dismiss();
                builder.setTitle("Edit Plant");

                LinearLayout.LayoutParams ar = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                editText.setLayoutParams(ar);
                builder.setView(editText);

                getText2(plotid, editText);

                builder.setPositiveButton("Edit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("plants", editText.getText().toString());

                                FirebaseDatabase.getInstance().getReference("Plants/"+id)
                                        .child(plotid).updateChildren(hashMap);
                                Intent intent = new  Intent(getApplicationContext(), Plants.class);
                                startActivity(intent);
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
            }
        });

        dialog.show();
    }

    public void getText(String plotid, final EditText editText){
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Plants/"+id)
        .child(plotid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Crops.class).getPlot());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getText2(String plotid, final EditText editText){
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Plants/"+id)
                .child(plotid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Crops.class).getPlants());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openPlot1() {
        Intent intent = new Intent(this, Plot1.class);
        startActivity(intent);
    }

    public void Return (View view){
        Intent intent = new  Intent(getApplicationContext(), Dashboard.class);
        startActivity(intent);
    }
}
