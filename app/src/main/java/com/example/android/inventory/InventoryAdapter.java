package com.example.android.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.Inventory;

import java.util.ArrayList;

public class InventoryAdapter extends ArrayAdapter<Inventory> {
    public InventoryAdapter(Context context, ArrayList<Inventory> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Inventory inventory = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.name);
        // Populate the data into the template view using the data object
        name.setText(inventory.getName());

        // Return the completed view to render on screen
        return convertView;

    }
}
