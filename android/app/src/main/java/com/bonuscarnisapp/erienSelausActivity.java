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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class erienSelausActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;

    private String erotinmerkki = ","; // CSV:n erotinmerkki!
    private ListView listView;

    private TextView textviewAiempiaEria;

    private File[] csvFiles;
    private File[] csvFiles2;
    private String[] csvFilenames;
    private Switch switchNaytaYhteenvetoina;
    private static final DecimalFormat dfEuro = new DecimalFormat("0.00");

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

        // Poistakaa kommentointi alta (rivit 66-89) ja ajakaa kerran, niin saatte poistettua kaikki vanhojen erien tiedostot. Muistakaa kommentoida takaisin!
        /*
        String csv_folderPath = getFilesDir().getAbsolutePath() + File.separator + "csv_tiedostot";
        File deleteFolder = new File(csv_folderPath);
        if (!deleteFolder.exists()) {
            deleteFolder.mkdirs();
        }
        File[] deleteFiles = deleteFolder.listFiles();
        for (int i = 0; i < deleteFiles.length; i++){
            if(deleteFiles[i].getName().endsWith(".csv")){
                deleteFiles[i].delete();
            }
        }
        String yhteenvedot_folderPath = getFilesDir().getAbsolutePath() + File.separator + "yhteenvedot";
        File deleteFolder2 = new File(csv_folderPath);
        if (!deleteFolder2.exists()) {
            deleteFolder2.mkdirs();
        }
        File[] deleteFiles2 = deleteFolder2.listFiles();
        for (int i = 0; i < deleteFiles2.length; i++){
            if(deleteFiles2[i].getName().endsWith(".csv")){
                deleteFiles2[i].delete();
            }
        }
        */
        // Poisto-osio loppuu!

        // Switch
        switchNaytaYhteenvetoina = findViewById(R.id.swNaytaYhteenvetoina);
        switchNaytaYhteenvetoina.setChecked(false); // Tällä switchin oletusasento: true/false

        // Alustetaan ListView.
        listView = findViewById(R.id.listView);
        csvFiles = getCSVFiles(); // Hakee tallennetut csv-tiedostot
        csvFiles2 = getCSVFiles2(); // Hakee tallennetut yhteenveto_*.csv-tiedostot

        // (Purkkaa...) Kopioidaan csvFiles-lista tiedestonimi-listaksi, jotta saadaan tiedostopolku ja .csv pääte pois
        csvFilenames = new String[csvFiles.length];
        for(int i = 0; i<csvFiles.length; i++) {
            File iteroitavaTiedosto = csvFiles[i];
            String tiedostonimi = iteroitavaTiedosto.getName();
            csvFilenames[i] = tiedostonimi.substring(0, tiedostonimi.length()-4); // Lopusta .csv pois
        }
        //Muunnetaan csv tiedoston tiedot listaksi
        //List<File> fileList = new ArrayList<>(Arrays.asList(csvFiles)); // normi csv:t
        //List<File> fileList2 = new ArrayList<>(Arrays.asList(csvFiles2)); // Yhteenveto csv:t
        List<String> filenameList = new ArrayList<>(Arrays.asList(csvFilenames));

        // Päivitetään textviewAiempiaEria
        textviewAiempiaEria = findViewById(R.id.tvAiempiaEria);
        textviewAiempiaEria.setText("Skannattuja eriä tallennettuna: " + filenameList.size() + " kpl");

        //Määritetään ArrayAdapter näyttämään tiedostonimiluettelo ListView näkymässä
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filenameList);
        listView.setAdapter(adapter);

        //Kun ListView näkymässä olevaa kohdetta painaa pitkään, voi poistaa ko. tiedoston.
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //File fileToDelete = fileList.get(position);
                String filenameToDelete = filenameList.get(position);
                // Päätellään poistettavat tiedostot nimen perusteella:
                File fileToDelete1 = fetchFile(1, filenameToDelete);
                File fileToDelete2 = fetchFile(2, filenameToDelete);
                // Alertdialog
                new AlertDialog.Builder(erienSelausActivity.this)
                        .setTitle("Poistetaanko erä: " + filenameToDelete.substring(0, filenameToDelete.length()-4))
                        .setMessage("Haluatko varmasti poistaa tämän erän tiedostot?")
                        .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (fileToDelete1.delete() && fileToDelete2.delete()) {
                                    filenameList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    textviewAiempiaEria.setText("Skannattuja eriä tallennettuna: " + filenameList.size() + " kpl"); // Päivitetään textviewAiempiaEria
                                    Toast.makeText(erienSelausActivity.this, "Erän tiedostot poistettu", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(erienSelausActivity.this, "VIRHE! Erän tiedostojen poistaminen ei onnistunut!", Toast.LENGTH_SHORT).show();
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
                //File selectedFile = fileList.get(position);
                String filenameToDelete = filenameList.get(position);
                File selectedFile;
                File csvFile = fetchFile(1, filenameToDelete); // Aina varsinainen csv (ei siis yhteenveto)
                // Switchin asennosta riippuen: Jos halutaan yhteenvetona
                if(switchNaytaYhteenvetoina.isChecked()){
                    selectedFile = fetchFile(2, filenameToDelete);
                // Jos halutaan tuotteittain
                } else {
                    selectedFile = fetchFile(1, filenameToDelete);
                }
                //Kutsutaan metodia, jolla pääsee käsiksi dataan ja poistamaan sitä
                viewAndEditCSVFile(selectedFile, csvFile);
            }
        });
    }

    /*
    Metodi, joka hakee poistettavan tiedoston annetun tiedostonimen perusteella
     */
    private File fetchFile(int kohdeKansio, String poistettavanTiedostonNimi){
        File palautettavaTiedosto = null;
        String deleteFromFolderPath;
        File deleteFromFolder;
        File[] filesInFolder;
        if(kohdeKansio == 1){
            deleteFromFolderPath = getFilesDir().getAbsolutePath() + File.separator + "csv_tiedostot";
            deleteFromFolder = new File(deleteFromFolderPath);
            if (!deleteFromFolder.exists()) {
                deleteFromFolder.mkdirs();
            }
            filesInFolder = deleteFromFolder.listFiles();
            for (int i = 0; i < filesInFolder.length; i++) {
                if (filesInFolder[i].getName().equals(poistettavanTiedostonNimi + ".csv")) {
                    palautettavaTiedosto = filesInFolder[i];
                    break;
                }
            }
        } else {
            deleteFromFolderPath = getFilesDir().getAbsolutePath() + File.separator + "yhteenvedot";
            deleteFromFolder = new File(deleteFromFolderPath);
            if (!deleteFromFolder.exists()) {
                deleteFromFolder.mkdirs();
            }
            filesInFolder = deleteFromFolder.listFiles();
            for (int i = 0; i < filesInFolder.length; i++) {
                if (filesInFolder[i].getName().equals("yhteenveto_" + poistettavanTiedostonNimi + ".csv")) {
                    palautettavaTiedosto = filesInFolder[i];
                    break;
                }
            }
        }
        return palautettavaTiedosto;
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

    //Metodi, jolla haetaan yhteenveto_*.csv-tiedostot yhteenvedot-kansiosta
    private File[] getCSVFiles2() {
        String folderPath = getFilesDir().getAbsolutePath() + File.separator + "yhteenvedot";
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

    //Metodi joka listaa tiedoston datan näkyviin.
    private void viewAndEditCSVFile(File selectedFile, File csvFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            String line;
            String[] splittedLine;
            List<String> lines = new ArrayList<>();
            reader.readLine(); // Luetaan csv:n otsikkorivi pois
            int tuotteidenMaara = 0;
            int tuotteidenYhteispaino = 0;
            double tuotteidenYhteisarvo = 0;

            while ((line = reader.readLine()) != null) {
                splittedLine = line.split(erotinmerkki);
                // Tulostus switchin perusteella:
                if(switchNaytaYhteenvetoina.isChecked()){
                    lines.add(splittedLine[0] + " - " + splittedLine[1] + " - " + splittedLine[2] + " kpl : " + splittedLine[3] + " g * " + splittedLine[4] + " €/kg = " + splittedLine[5] + " €");
                    tuotteidenMaara = tuotteidenMaara + Integer.parseInt(splittedLine[2]);
                    tuotteidenYhteispaino = tuotteidenYhteispaino + Integer.parseInt(splittedLine[3]);
                    tuotteidenYhteisarvo = tuotteidenYhteisarvo + Double.parseDouble(splittedLine[5]);
                } else {
                    lines.add(splittedLine[0] + " - " + splittedLine[1] + " : " + splittedLine[2] + " g * " + splittedLine[3] + " €/kg = " + splittedLine[4] + " €");
                    tuotteidenMaara++;
                    tuotteidenYhteispaino = tuotteidenYhteispaino + Integer.parseInt(splittedLine[2]);
                    tuotteidenYhteisarvo = tuotteidenYhteisarvo + Double.parseDouble(splittedLine[4]);
                }
            }
            reader.close();
            String tulostettavaNimi = csvFile.getName().substring(0, csvFile.getName().length()-4);
            if(csvFile.getName().length() > 35){
                tulostettavaNimi = csvFile.getName().substring(0, 35) + " ...";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(erienSelausActivity.this);
            builder.setTitle("Erä: " + tulostettavaNimi + "\nTuotteita " + tuotteidenMaara + " kpl, yht. " + Double.toString(((double) tuotteidenYhteispaino)/1000) + " kg, " + dfEuro.format(tuotteidenYhteisarvo) + " €");
            ListView dataListView = new ListView(erienSelausActivity.this);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(erienSelausActivity.this, android.R.layout.simple_list_item_1, lines);
            dataListView.setAdapter(dataAdapter);
            builder.setView(dataListView);

            // Painamalla valittua dataa, tulee infoa että poistaminen täytyy tehdä skanneritilassa (poistaminen ei siis onnistu enää)
            dataListView.setOnItemClickListener((parent, view, position, id) -> {
                new AlertDialog.Builder(erienSelausActivity.this)
                        .setTitle("Huomio")
                        .setMessage("Jos haluat muokata erän sisältöä, se täytyy tehdä skanneritilassa. Klikkaa 'Muut toiminnot' -> 'Jatka'")
                        .setPositiveButton("OK", null)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .show();
            });

            builder.setNegativeButton("Palaa", null);
            /*
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
            });*/

            //builder.setNegativeButton("Peruuta", null);

            // Mahdollistetaan muut toiminnot
            builder.setNeutralButton("Muut toiminnot", (dialog, which) -> {
                // Luodaan uusi alert dialog
                AlertDialog.Builder builder2 = new AlertDialog.Builder(erienSelausActivity.this);
                builder2.setTitle("Muut vaihtoehdot");
                builder2.setMessage("Jatketaanko erän skannausta tai lähetetäänkö erän tiedot CSV-muodossa osoitteeseen " + sharedPref.getString("defaultEmail", null) + "?");

                // Lisätään painikkeet
                builder2.setPositiveButton("Lähetä", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tiedostonimi = csvFile.getName(); // Muodostetaan tiedostonimi polun avulla
                        // Lähetetään sähköpostiin halutut tiedostot
                        String[] liitetiedostot = {tiedostonimi};
                        // Muodostetaan viestin aihe
                        String viestinAihe = "Joku hieno nimi: Erän " + tiedostonimi + " tiedot";
                        String viestinSisalto = "Tämä on sovelluksen lähettämä viesti.\n\nHalutun erän skannaustiedot löytyvät liitteinä olevista '" + tiedostonimi + "' ja 'yhteenveto_" + tiedostonimi +"' tiedostoista.";

                        // Luodaan Sahkopostiviesti-olio (parametreina context, viestin aihe ja sisältö)
                        Sahkopostiviesti sahkopostiviesti = new Sahkopostiviesti(getApplicationContext(), viestinAihe, viestinSisalto, liitetiedostot);
                        // Lähetetään sähköpostiviesti (parametrina view ja liitetiedosto)
                        sahkopostiviesti.lahetaSahkopostiviesti(findViewById(android.R.id.content));
                    }
                });
                // Negative-buttoniin erän jatkaminen
                builder2.setNegativeButton("Jatka", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tiedostonimi = csvFile.getName(); // Muodostetaan tiedostonimi polun avulla
                        // Avataan skanneri ja välitetään tiedostonimi parametrina
                        Intent intent = new Intent(erienSelausActivity.this, eranSkannausActivity.class);
                        intent.putExtra("eranTiedostonimiAvain", tiedostonimi );
                        startActivity(intent);


                    }
                });
                // Peruuta sähköpostin lähetys
                builder2.setNeutralButton("Peruuta", null);
                // Dialogi näkyviin
                AlertDialog dialog2 = builder2.create();
                dialog2.show();
            });



            // Ikkuna näkyviin
            builder.show();
        } catch (IOException e) {
            Toast.makeText(erienSelausActivity.this, "Virhe tiedoston lukemisessa", Toast.LENGTH_SHORT).show();
        }
    }

}