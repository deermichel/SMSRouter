package de.mh.smsrouter;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ev.contactsmultipicker.ContactPickerActivity;
import com.ev.contactsmultipicker.ContactResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class GroupSettings extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    // contact picker result
    public final int PICK_CONTACT = 1000;

    // gui objects
    private ListView listNumbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        // activate back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set title
        setTitle(getIntent().getExtras().getString("name"));

        // get gui objects
        listNumbers = (ListView) findViewById(R.id.list_numbers);

        // setup numbers list
        listNumbers.setOnItemClickListener(this);
        listNumbers.setOnItemLongClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // add number
            case R.id.action_add_number:
                Intent intent = new Intent(this, ContactPickerActivity.class);
                startActivityForResult(intent, PICK_CONTACT);
                return true;

            // back button in action bar pressed
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, R.string.group_settings_note, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        // delete number

        // get stored numbers
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("group_" + getTitle().toString(), new HashSet<String>());
        Set<String> numbers = new HashSet<>(stored);

        // remove number
        numbers.remove(listNumbers.getItemAtPosition(position).toString());

        // save numbers
        prefs.edit().putStringSet("group_" + getTitle().toString(), numbers).apply();

        onResume();

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get numbers
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("group_" + getTitle().toString(), new HashSet<String>());
        final ArrayList<String> numbers = new ArrayList<>(stored);
        Collections.sort(numbers);

        // update listview
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, numbers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // get gui objects
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                // set text
                text1.setText(numbers.get(position).split(";")[0]);
                text2.setText(numbers.get(position).split(";")[1]);

                return view;
            }
        };
        listNumbers.setAdapter(listAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {

            // selected contacts
            ArrayList<ContactResult> contacts = (ArrayList<ContactResult>) data.getSerializableExtra(ContactPickerActivity.CONTACT_PICKER_RESULT);
            for (ContactResult contact : contacts) {
                for (ContactResult.ResultItem item : contact.getResults()) {
                    String number = item.getResult();

                    // get stored numbers
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    Set<String> stored = prefs.getStringSet("group_" + getTitle().toString(), new HashSet<String>());
                    Set<String> numbers = new HashSet<>(stored);

                    // add number
                    numbers.add(getContactName(number) + ";" + number);

                    // save numbers
                    prefs.edit().putStringSet("group_" + getTitle().toString(), numbers).apply();

                }
            }

            onResume();
        }

    }

    /** gets contact name by phone number */
    public String getContactName(String number) {

        ContentResolver cr = getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) return null;
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if(!cursor.isClosed()) cursor.close();

        return contactName;
    }

}
