package com.example.vjezba4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ResultsAdapter extends android.widget.ArrayAdapter<String> {

    public ResultsAdapter(Context context, ArrayList<String> results) {
        super(context, 0, results);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String result = getItem(position);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(result);

        return convertView;
    }
}
