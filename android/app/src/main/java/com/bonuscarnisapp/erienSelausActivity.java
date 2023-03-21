package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class erienSelausActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;

    private ListView listView;

    private File[] csvFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aseta teema
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        isDarkTheme = sharedPref.getBoolean("darkTheme", false);
        if (isDarkTheme) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
        setContentView(R.layout.activity_erien_selaus);

        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Erien selaus");


        // Alustetaan ListView.
        listView = findViewById(R.id.listView);
        csvFiles = getCSVFiles();

        //Muunnetaan csv tiedoston tiedot listaksi
        List<File> fileList = new ArrayList<>(Arrays.asList(csvFiles));

        //Määritetään ArrayAdapter näyttämään tiedostoluettelo ListView näkymässä
        ArrayAdapter<File> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, csvFiles);
        listView.setAdapter(adapter);

        //Kun ListView näkymässä olevaa kohdetta painaa pitkään, voi poistaa ko. tiedoston.
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                File fileToDelete = fileList.get(position);

                new AlertDialog.Builder(erienSelausActivity.this)
                        .setTitle("Poista tiedosto")
                        .setMessage("Haluatko varmasti poistaa tämän tiedoston?")
                        .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (fileToDelete.delete()) {
                                    fileList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(erienSelausActivity.this, "Tiedosto poistettu", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(erienSelausActivity.this, "VIRHE! tiedostoa ei voitu poistaa", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Ei", null)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .show();

                return true;
            }
        });

    }

    //Metodi, jolla haetaan csv-tiedostot csv.tiedostot-kansiosta
    private File[] getCSVFiles() {
        String folderPath = getFilesDir().getAbsolutePath() + File.separator + "csv_tiedostot";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder.listFiles((dir, name) -> name.endsWith(".csv"));
    }


    /*
    Metodi, joka palauttaa ohjelman päävalikkoon, kun käyttäjä painaa ActionBarissa olevaa nuolta.
    Ei kysy käyttäjältä erillistä varmistusta.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Palaa alkuvalikkoon
                Intent intent = new Intent(erienSelausActivity.this, MainActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}