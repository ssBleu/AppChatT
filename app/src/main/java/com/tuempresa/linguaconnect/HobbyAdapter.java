package com.tuempresa.linguaconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

public class HobbyAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] hobbies;
    private int[] images;

    public HobbyAdapter(Context context, String[] hobbies, int[] images) {
        super(context, 0, hobbies);
        this.context = context;
        this.hobbies = hobbies;
        this.images = images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_hobbie, parent, false);
        }

        TextView hobbyName = convertView.findViewById(R.id.hobbyName);
        ImageView hobbyImage = convertView.findViewById(R.id.hobbyImage);

        hobbyName.setText(hobbies[position]);
        hobbyImage.setImageResource(images[position]);

        return convertView;
    }
}
