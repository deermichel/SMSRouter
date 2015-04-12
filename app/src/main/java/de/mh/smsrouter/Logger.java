package de.mh.smsrouter;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    /** gets logfile */
    public static File getLogfile() {
        File appFolder = new File(Environment.getExternalStorageDirectory() + "/SMSRouter/");
        appFolder.mkdir();
        return new File(appFolder, "logfile.log");
    }

    /** logs a message */
    public static void log(String message) throws IOException {

        // read log
        File logfile = getLogfile();
        String log = Files.read(logfile);

        // add message
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log = sdf.format(new Date()) + " " + message + "\n" + log;

        // save log
        Files.write(logfile, log);

    }

}
