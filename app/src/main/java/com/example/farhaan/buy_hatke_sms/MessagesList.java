package com.example.farhaan.buy_hatke_sms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Farhaan on 02-07-2016.
 */

public class MessagesList extends Activity {

    private static MessagesList inst1;
    ListView messageListView;
    TextView Name;
    ArrayList<String> messageList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;

    public static MessagesList instance() {
        return inst1;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst1 = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_list);
        Name = (TextView) findViewById(R.id.Name);
        messageListView = (ListView) findViewById(R.id.messageList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(arrayAdapter);
        Name.setText(Values.user);

        addMessages();
    }

    public void addMessages(){
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");

        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        do {
            String str = smsInboxCursor.getString(indexAddress);
            if (str.equals(Values.user)){
                arrayAdapter.add(smsInboxCursor.getString(indexBody));
            }
        } while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String Message){
        arrayAdapter.insert(Message, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void goToCompose(View view){
        Intent intent = new Intent(MessagesList.this, SendSmsActivity.class);
        startActivity(intent);
    }
}
