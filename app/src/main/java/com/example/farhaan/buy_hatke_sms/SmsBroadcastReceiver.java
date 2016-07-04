package com.example.farhaan.buy_hatke_sms;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Farhaan on 02-07-2016.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if(intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            String smsBodyStr = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();
                long timeMillis = smsMessage.getTimestampMillis();

                Date date = new Date(timeMillis);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                String dateText = format.format(date);

                smsMessageStr += address;
                smsBodyStr += smsBody;
                Values.user = address;
                //smsMessageStr += smsBody + "\n";
            }

            //Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(context.getApplicationContext(), MessagesList.class);
            final PendingIntent pIntent = PendingIntent.getActivity(context.getApplicationContext(), (int) System.currentTimeMillis(), intent1, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(R.drawable.new_messages_red);
            mBuilder.setContentTitle(smsMessageStr);
            mBuilder.setContentText(smsBodyStr);
            mBuilder.setAutoCancel(true);
            mBuilder.setContentIntent(pIntent);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());

            //this will update the UI with message
            ReceiveSmsActivity inst = ReceiveSmsActivity.instance();
            inst.updateList(smsMessageStr);

            if (smsMessageStr.equals(Values.user)){
                MessagesList inst1 = MessagesList.instance();
                inst1.updateList(smsBodyStr);
            }
        }
    }
}
