package de.mh.smsrouter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class EditGroups extends ActionBarActivity implements AdapterView.OnItemClickListener {

    // gui objects
    private ListView listGroups;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_groups);

        // activate back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get gui objects
        listGroups = (ListView) findViewById(R.id.list_groups);

        // setup groups list
        listGroups.setOnItemClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // new group
            case R.id.action_new_group:
                showNewGroupDialog();
                return true;

            // back button in action bar pressed
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    /** shows new group dialog */
    private void showNewGroupDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_new_group, null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // check input
                if (((EditText) dialogView.findViewById(R.id.dialog_new_group_text_name)).getText().toString().equals("")) {
                    showNewGroupDialog();
                } else {
                    newGroup(((EditText) dialogView.findViewById(R.id.dialog_new_group_text_name)).getText().toString());
                }
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

    }

    /** creates new group */
    private void newGroup(String name) {

        // get stored groups
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("groups", new HashSet<String>());
        Set<String> groups = new HashSet<>(stored);

        // add group
        groups.add(name);

        // save groups
        prefs.edit().putStringSet("groups", groups).apply();

        // open group settings
        Intent intent = new Intent(this, GroupSettings.class);
        intent.putExtra("name", name);
        startActivity(intent);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        // create options dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.group_options_dialog, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    // open group settings
                    case 0:
                        Intent intent = new Intent(EditGroups.this, GroupSettings.class);
                        intent.putExtra("name", listGroups.getItemAtPosition(position).toString());
                        startActivity(intent);
                        break;

                    // rename
                    case 1:
                        showRenameDialog(listGroups.getItemAtPosition(position).toString());
                        break;

                    // delete
                    case 2:

                        // get stored groups
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EditGroups.this);
                        Set<String> stored = prefs.getStringSet("groups", new HashSet<String>());
                        Set<String> groups = new HashSet<>(stored);

                        // remove group
                        groups.remove(listGroups.getItemAtPosition(position).toString());

                        // save groups
                        prefs.edit().putStringSet("groups", groups).apply();

                        // remove group settings
                        prefs.edit().remove("group_" + listGroups.getItemAtPosition(position).toString()).apply();

                        onResume();
                        break;

                }

            }

        });
        builder.show();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // get groups
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("groups", new HashSet<String>());
        ArrayList<String> groups = new ArrayList<>(stored);
        Collections.sort(groups);

        // update listview
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groups);
        listGroups.setAdapter(listAdapter);

    }

    /** shows dialog for rename group */
    private void showRenameDialog(final String name) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_rename, null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // check input
                if (((EditText) dialogView.findViewById(R.id.dialog_rename_text_name)).getText().toString().equals("")) {
                    showRenameDialog(name);
                } else {
                    rename(name, ((EditText) dialogView.findViewById(R.id.dialog_rename_text_name)).getText().toString());
                }
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

    }

    /** renames a group */
    private void rename(String name, String newName) {

        // get stored groups
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EditGroups.this);
        Set<String> stored = prefs.getStringSet("groups", new HashSet<String>());
        Set<String> groups = new HashSet<>(stored);

        // rename group
        groups.add(newName);
        groups.remove(name);

        // save groups
        prefs.edit().putStringSet("groups", groups).apply();

        // update group settings
        Set<String> group = prefs.getStringSet("group_" + name, new HashSet<String>());
        prefs.edit().remove("group_" + name).apply();
        prefs.edit().putStringSet("group_" + newName, group).apply();

        onResume();
    }

}
