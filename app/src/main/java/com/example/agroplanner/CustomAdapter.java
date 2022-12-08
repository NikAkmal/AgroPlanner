package com.example.agroplanner;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView plant,plot;
    public ImageView plantpicture;

    public CustomAdapter(@NonNull View itemView) {
        super(itemView);
        plant = (TextView)itemView.findViewById(R.id.plant);
        plot = (TextView)itemView.findViewById(R.id.plot);
        plantpicture = (ImageView) itemView.findViewById(R.id.plantpicture);
    }

    @Override
    public void onClick(View v) {

    }
}