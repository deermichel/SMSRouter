package de.mh.smsrouter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/** file operations */
public class Files {

    /** reads text from a given file */
    public static String read(File file) throws IOException {

        // create file if it does not exist
        file.createNewFile();

        StringBuilder text = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = reader.readLine()) != null) text.append(line + "\n");
        if (text.length() > 0) text.deleteCharAt(text.length() - 1);
        reader.close();

        return text.toString();
    }

    /** writes text to the given file */
    public static void write(File file, String text) throws IOException {

        FileOutputStream output = new FileOutputStream(file);
        output.write(text.getBytes());
        output.flush();
        output.close();

    }

}
