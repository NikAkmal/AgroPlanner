package com.example.agroplanner;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView description,fertilizer;
    public ImageView plantpicture;
    public Button delete, edit;

    public CalendarAdapter(@NonNull View itemView) {
        super(itemView);
        description = (TextView)itemView.findViewById(R.id.description);
        fertilizer = (TextView)itemView.findViewById(R.id.fertilizer);
        plantpicture = (ImageView) itemView.findViewById(R.id.plantpicture);
        delete =(Button) itemView.findViewById(R.id.deleteR);
        edit =(Button) itemView.findViewById(R.id.editR);

    }

    @Override
    public void onClick(View v) {

    }
}