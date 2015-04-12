package de.mh.smsrouter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class Main extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    // gui objects
    private ListView listTags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get gui objects
        listTags = (ListView) findViewById(R.id.list_tags);

        // setup groups list
        listTags.setOnItemClickListener(this);
        listTags.setOnItemLongClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // new tag
            case R.id.action_new:
                showNewTagDialog();
                return true;

            // edit groups
            case R.id.action_edit_groups:
                startActivity(new Intent(this, EditGroups.class));
                return true;

            // show log
            case R.id.action_view_log:
                startActivity(new Intent(this, ViewLog.class));
                return true;

            // show about
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.about);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    /** shows new tag dialog */
    private void showNewTagDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_new_tag, null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // check input
                if (((EditText) dialogView.findViewById(R.id.dialog_new_tag_text_name)).getText().toString().equals("")) {
                    showNewTagDialog();
                } else {
                    newTag(((EditText) dialogView.findViewById(R.id.dialog_new_tag_text_name)).getText().toString());
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

    /** creates new tag */
    private void newTag(String name) {

        // get stored tags
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("tags", new HashSet<String>());
        Set<String> tags = new HashSet<>(stored);

        // add tag
        tags.add(name);

        // save tags
        prefs.edit().putStringSet("tags", tags).apply();

        // open tag settings
        showTagSettingsDialog(name);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // open tag settings
        showTagSettingsDialog(listTags.getItemAtPosition(position).toString());

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        // create options dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.options_dialog, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    // rename
                    case 0:
                        showRenameDialog(listTags.getItemAtPosition(position).toString());
                        break;

                    // delete
                    case 1:

                        // get stored tags
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
                        Set<String> stored = prefs.getStringSet("tags", new HashSet<String>());
                        Set<String> tags = new HashSet<>(stored);

                        // remove tag
                        tags.remove(listTags.getItemAtPosition(position).toString());

                        // save tags
                        prefs.edit().putStringSet("tags", tags).apply();

                        // remove tag settings
                        prefs.edit().remove("tag_" + listTags.getItemAtPosition(position).toString()).apply();

                        onResume();
                        break;

                }

            }

        });
        builder.show();

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get tags
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("tags", new HashSet<String>());
        ArrayList<String> tags = new ArrayList<>(stored);
        Collections.sort(tags);

        // update listview
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tags);
        listTags.setAdapter(listAdapter);

    }

    /** shows dialog for rename tag */
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

    /** renames a tag */
    private void rename(String name, String newName) {

        // get stored tags
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
        Set<String> stored = prefs.getStringSet("tags", new HashSet<String>());
        Set<String> tags = new HashSet<>(stored);

        // rename tag
        tags.add(newName);
        tags.remove(name);

        // save tags
        prefs.edit().putStringSet("tags", tags).apply();

        // update tag settings
        Set<String> group = prefs.getStringSet("tag_" + name, new HashSet<String>());
        prefs.edit().remove("tag_" + name).apply();
        prefs.edit().putStringSet("tag_" + newName, group).apply();

        onResume();
    }

    /** shows dialog for tag settings */
    private void showTagSettingsDialog(final String name) {

        // get groups
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stored = prefs.getStringSet("groups", new HashSet<String>());
        final ArrayList<String> groups = new ArrayList<>(stored);
        Collections.sort(groups);

        // get activated groups
        Set<String> activatedStored = prefs.getStringSet("tag_" + name, new HashSet<String>());
        final ArrayList<String> activatedGroups = new ArrayList<>(activatedStored);

        // create boolean array
        boolean activated[] = new boolean[groups.size()];
        for (int i = 0; i < activated.length; i++) {
            if (activatedGroups.contains(groups.get(i))) {
                activated[i] = true;
            } else {
                activated[i] = false;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_groups);
        builder.setMultiChoiceItems(groups.toArray(new String[groups.size()]), activated, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    activatedGroups.add(groups.get(which));
                } else {
                    activatedGroups.remove(groups.get(which));
                }
            }
        });
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.edit().putStringSet("tag_" + name, new HashSet<>(activatedGroups)).apply();
                onResume();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onResume();
            }
        });

        builder.show();
    }

}
