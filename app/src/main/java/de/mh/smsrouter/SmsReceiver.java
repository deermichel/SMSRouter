package de.mh.smsrouter;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) return;

        // enabled?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("enabled", true)) return;

        // get bundle
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        // read sms
        String from = "";
        String message = "";
        try {

            Object[] pdus = (Object[])bundle.get("pdus");
            for (Object pdu : pdus) {

                // parse sms
                SmsMessage sms = SmsMessage.createFromPdu((byte[])pdu);
                if (!from.contains(sms.getDisplayOriginatingAddress())) from += sms.getDisplayOriginatingAddress() + "; ";
                message += sms.getDisplayMessageBody();

            }
            from = from.substring(0, from.length() - 2);

            // check if message contains tag
            if (!message.startsWith("(@")) return;

            // get stored tags
            Set<String> tags = prefs.getStringSet("tags", new HashSet<String>());

            // parse tag
            String tag = "";
            for (int i = 2; i < message.length(); i++) {
                if (message.charAt(i) == ')') break;
                tag += message.charAt(i);
            }

            // parse text
            String text = message.substring(tag.length() + 3);

            // check if tag exists
            if (!tags.contains(tag)) throw new Exception("Unknown tag: " + tag);

            // check if tag enabled
            if (!prefs.getBoolean("setting_0_" + tag, true)) return;

            // get groups of tag
            Set<String> groups = prefs.getStringSet("tag_" + tag, new HashSet<String>());

            // get numbers of groups
            ArrayList<String> numbers = new ArrayList<>();
            for (String group : groups) numbers.addAll(prefs.getStringSet("group_" + group, new HashSet<String>()));

            // check if message needs moderation
            if (prefs.getBoolean("setting_1_" + tag, false)) {
                showModerationDialog(context, numbers, text, from, tag);
                return;
            }

            // send sms
            SmsManager smsManager = SmsManager.getDefault();
            for (String number : numbers) smsManager.sendTextMessage(number.split(";")[1], null, text, null, null);

            // create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(context.getText(R.string.app_name));
            builder.setContentText(context.getString(R.string.notification, from, tag, numbers.size()));
            Intent showLog = new Intent(context, ViewLog.class);
            PendingIntent resultIntent = PendingIntent.getActivity(context, 0, showLog, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultIntent);

            // show notification
            NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int id = prefs.getInt("id", 0);
            notifyMgr.notify(id, builder.build());
            prefs.edit().putInt("id", id + 1).apply();

            // log
            Logger.log(context.getString(R.string.notification, from, tag, numbers.size()));

        } catch (Exception e) {
            try {
                Logger.log(context.getText(R.string.error) + e.toString());

                // create notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle(context.getText(R.string.app_name));
                builder.setContentText(context.getText(R.string.error) + e.toString());
                Intent showLog = new Intent(context, ViewLog.class);
                PendingIntent resultIntent = PendingIntent.getActivity(context, 0, showLog, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultIntent);

                // show notification
                NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notifyMgr.notify(-1, builder.build());

            } catch (Exception e2) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            }
        }

    }

    /** shows dialog for moderate messages */
    private void showModerationDialog(final Context context, final ArrayList<String> numbers, final String text, final String from, final String tag) {

        Intent intent = new Intent(context, ModerationDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("numbers", numbers);
        intent.putExtra("text", text);
        intent.putExtra("from", from);
        intent.putExtra("tag", tag);
        context.startActivity(intent);

    }
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.moderation);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                // send sms
                SmsManager smsManager = SmsManager.getDefault();
                for (String number : numbers)
                    smsManager.sendTextMessage(number.split(";")[1], null, text, null, null);

                // create notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle(context.getText(R.string.app_name));
                builder.setContentText(context.getString(R.string.notification, from, tag, numbers.size()));
                Intent showLog = new Intent(context, ViewLog.class);
                PendingIntent resultIntent = PendingIntent.getActivity(context, 0, showLog, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultIntent);

                // show notification
                NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                int id = prefs.getInt("id", 0);
                notifyMgr.notify(id, builder.build());
                prefs.edit().putInt("id", id + 1).apply();

                // log
                try {
                    Logger.log(context.getString(R.string.notification, from, tag, numbers.size()));
                } catch (IOException e) {
                }

            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }
*/
}
