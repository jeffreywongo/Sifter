package com.example.sifter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * Created by jeffreywongo on 9/12/2017.
 */


class CustomAdapter extends ArrayAdapter<String> {

    public CustomAdapter(Context context) {
        super(context, R.layout.custom_row);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflater is helps pass in an xml file into a view
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.custom_row, parent, false);
        // get item at given position in the adapter or backing array
        String info = getItem(position);
        String sender;
        String subject;
        Log.d("MESSAGE", info);
        assert info != null;
        if (info.contains("<")) {
            sender = info.substring(0, info.indexOf("<") - 1);
            subject = info.substring(info.indexOf("|") + 1, info.length());
        } else {
            sender = info.substring(0, info.indexOf("|") - 1);
            subject = info.substring(info.indexOf("|") + 1, info.length());
        }
        // pass the strings into their respective textviews
        TextView subjectText = (TextView) customView.findViewById(R.id.subject);
        TextView senderText = (TextView) customView.findViewById(R.id.sender);
        subjectText.setText(subject);
        senderText.setText(sender);
        return customView;
    }
}
