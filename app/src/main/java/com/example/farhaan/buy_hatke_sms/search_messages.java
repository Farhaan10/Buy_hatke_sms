package com.example.farhaan.buy_hatke_sms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Farhaan on 02-07-2016.
 */
public class search_messages extends Activity implements AdapterView.OnItemClickListener{

    ListView lv;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> search_messages = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_messages);
        lv =(ListView) findViewById(R.id.search_list);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, search_messages);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(this);

        searchList();
    }

    public void searchList(){

        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");

        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        do {
            String str = smsInboxCursor.getString(indexBody) + "\n" + smsInboxCursor.getString(indexAddress);
                arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());

        arrayAdapter.getFilter().filter(Values.query);

    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
        try {
            Values.user = arrayAdapter.getItem(pos).split("\n")[1];
            System.out.println(Values.user);
            Intent intent = new Intent(search_messages.this, MessagesList.class);
            startActivity(intent);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
