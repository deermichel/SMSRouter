package de.mh.smsrouter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;

import java.io.IOException;
import java.util.ArrayList;

public class ModerationDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<String> numbers = getIntent().getExtras().getStringArrayList("numbers");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.moderation, getIntent().getExtras().getString("from"), getIntent().getExtras().getString("tag"), numbers.size(), getIntent().getExtras().getString("text")));
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ModerationDialog.this);

                // send sms
                SmsManager smsManager = SmsManager.getDefault();
                for (String number : numbers)
                    smsManager.sendTextMessage(number.split(";")[1], null, getIntent().getExtras().getString("text"), null, null);

                // create notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ModerationDialog.this);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle(getText(R.string.app_name));
                builder.setContentText(getString(R.string.notification, getIntent().getExtras().getString("from"), getIntent().getExtras().getString("tag"), numbers.size()));
                Intent showLog = new Intent(ModerationDialog.this, ViewLog.class);
                PendingIntent resultIntent = PendingIntent.getActivity(ModerationDialog.this, 0, showLog, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultIntent);

                // show notification
                NotificationManager notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                int id = prefs.getInt("id", 0);
                notifyMgr.notify(id, builder.build());
                prefs.edit().putInt("id", id + 1).apply();

                // log
                try {
                    Logger.log(getString(R.string.notification, getIntent().getExtras().getString("from"), getIntent().getExtras().getString("tag"), numbers.size()));
                } catch (IOException e) {
                }

                finish();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();
    }

}
