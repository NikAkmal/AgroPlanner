package com.example.agroplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class CalendarDisplay extends AppCompatActivity {

    private TextView theDate;
    private Button btnGoCalendar;
    String plantName, plotName, plotid, date;
    int day, month, year;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Calendar, CalendarAdapter> recyclerAdapter;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    Dialog dialog;
    private Button cancel, edit;
    EditText editText, editText2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_display);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            date = extras.getString("date");
            day = extras.getInt("day");
            month = extras.getInt("month");
            year = extras.getInt("year");
            plantName = extras.getString("plantName");
            plotName = extras.getString("plotName");
            plotid = extras.getString("plotid");
        }

        theDate = (TextView) findViewById(R.id.date);

        btnGoCalendar = (Button) findViewById(R.id.date_button);

        //Firebase init
        database = FirebaseDatabase.getInstance();
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = database.getReference()
        .child("Record/" +plotid+"/Daily/" +day +"/" +month +"/" +year);
        dialog = new Dialog(this);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);

        loadData();

        theDate.setText(date);

        btnGoCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CalendarDisplay.this, CalendarActivity.class);
                Bundle extras = new Bundle();
                extras.putString("plantName", plantName);
                extras.putString("plotName", plotName);
                extras.putString("plotid", plotid);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Calendar>()
                        .setQuery(databaseReference,Calendar.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Calendar, CalendarAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CalendarAdapter holder, int position, @NonNull Calendar model) {

                int sday = model.getDay();
                int syear = model.getYear();
                int smonth = model.getMonth();

                String description = model.getDescription();
                String fertilizer = model.getFertilizer();
                String imageUrl = model.getImageUrl();

                String id = model.getId();
                String postid = model.getPostid();
                String type = model.getType();

                holder.description.setText("Daily Description: " + model.getDescription());
                holder.fertilizer.setText("Fertilizer: " + model.getFertilizer());

                Glide.with(getApplicationContext()).load(model.getImageUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                    .into(holder.plantpicture);

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder   builder = new AlertDialog.Builder(CalendarDisplay.this);
                        builder.setCancelable(true);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure you want to delete this Record?");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //remove Record
                                FirebaseDatabase.getInstance().getReference("Record/" +plotid+"/Daily/" +day +"/" +month +"/" +year)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
//                                      Delete Image in Storage Code
                                            StorageReference photoDelete = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                            photoDelete.delete();
                                            Toast.makeText(CalendarDisplay.this, "Deleted!", Toast.LENGTH_SHORT).show();
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

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        openDialog(description, fertilizer, postid);
                    }
                });

            }

            @NonNull
            @Override
            public CalendarAdapter onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.calendar,viewGroup,false);
                return new CalendarAdapter(view);
            }};

        recyclerAdapter.notifyDataSetChanged();
        recyclerAdapter.startListening();
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void openDialog(String description, String fertilizer, String postid) {
        dialog.setContentView(R.layout.calender_dialog);

        cancel = dialog.findViewById(R.id.cancelE);
        edit = dialog.findViewById(R.id.edit_button);
        editText = dialog.findViewById(R.id.descriptionE);
        editText2 = dialog.findViewById(R.id.fertilizerE);

        getText(postid, editText, editText2);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("description", editText.getText().toString());
                hashMap.put("fertilizer", editText2.getText().toString());

                FirebaseDatabase.getInstance().getReference("Record/" +plotid+"/Daily/" +day +"/" +month +"/" +year)
                        .child(postid).updateChildren(hashMap);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        dialog.show();

    }


    private void getText(String postid, EditText editText, EditText editText2) {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Record/" +plotid+"/Daily/" +day +"/" +month +"/" +year)
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Calendar.class).getDescription());
                editText2.setText(snapshot.getValue(Calendar.class).getFertilizer());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void Return (View view){
        Intent intent = new  Intent(getApplicationContext(), Plants.class);
        startActivity(intent);
    }
}