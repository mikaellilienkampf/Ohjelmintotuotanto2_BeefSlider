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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

        //Valittua erää kerran painamalla pääsee käsiksi erän sisältämään dataan.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File selectedFile = fileList.get(position);
                //Kutsutaan metodia, jolla pääsee käsiksi dataan ja poistamaan sitä
                viewAndEditCSVFile(selectedFile);
            }
        });


/////////////////


    }

   //////////////////

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

    //Metodi joka listaa tiedoston datan näkyviin ja niitä voi poistaa.
    private void viewAndEditCSVFile(File selectedFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            String line;
            List<String> lines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();


            AlertDialog.Builder builder = new AlertDialog.Builder(erienSelausActivity.this);
            builder.setTitle("Tiedoston sisältö");

            ListView dataListView = new ListView(erienSelausActivity.this);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(erienSelausActivity.this, android.R.layout.simple_list_item_1, lines);
            dataListView.setAdapter(dataAdapter);
            builder.setView(dataListView);

            // Painamalla valittua dataa, sen voi poistaa
            dataListView.setOnItemClickListener((parent, view, position, id) -> {

                new AlertDialog.Builder(erienSelausActivity.this)
                        .setTitle("Poista tieto")
                        .setMessage("Haluatko varmasti poistaa tämän tiedon?")
                        .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                lines.remove(position);
                                dataAdapter.notifyDataSetChanged();
                            }
            })

                        .setNegativeButton("Ei", null)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .show();
            });

            // Muutosten tallennus
            builder.setPositiveButton("Tallenna", (dialog, which) -> {
                try {
                    FileWriter writer = new FileWriter(selectedFile, false);
                    for (String rowData : lines) {
                        writer.write(rowData + "\n");
                    }
                    writer.close();
                } catch (IOException e) {
                    Toast.makeText(erienSelausActivity.this, "Virhe tiedoston tallennuksessa", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Peruuta", null);
            builder.show();
        } catch (IOException e) {
            Toast.makeText(erienSelausActivity.this, "Virhe tiedoston lukemisessa", Toast.LENGTH_SHORT).show();
        }
    }



}