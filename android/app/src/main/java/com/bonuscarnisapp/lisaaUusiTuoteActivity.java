package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class lisaaUusiTuoteActivity extends AppCompatActivity {

    private Button bt_hae;
    private Button bt_poista;
    private Button tallenna;

    private Button bt_paivita;
    private EditText textAnnaEan;
    private EditText textAnnaTuoteNimi;
    private EditText textAnnaHinta;
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;

    static ArrayList<Tuote> tuoteArrayList = new ArrayList<Tuote>();


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
        setContentView(R.layout.activity_lisaa_uusi_tuote);


        //Lataa tiedostossa olevat tiedot käyttöön
        try {
        lataaTiedot();
        } catch (Exception e) {
            String[] line = Tuote.alkuData().split("\n");

            for (String l : line) {
                String[] parts = l.split(";");
                int id = Integer.parseInt(parts[0]);
                String nimi = parts[1];
                float hinta = Float.parseFloat(parts[2]);
                Tuote tuote = new Tuote(id, nimi, hinta);
                tuoteArrayList.add(tuote);
            }
            tallennaTiedot();
        }


        // Paluu-painike Actionbariin
        ActionBar actionBar = getSupportActionBar();
        // ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Lisää ja muokkaa tuotteita");

        // Nimi-muuttujat talteen
        textAnnaEan = findViewById(R.id.textAnnaEan);
        textAnnaTuoteNimi = findViewById(R.id.textAnnaTuoteNimi);
        textAnnaHinta = findViewById(R.id.textAnnaHinta);
        tallenna = findViewById(R.id.tallennaButton);
        bt_poista = findViewById(R.id.bt_poista);
        bt_hae = findViewById(R.id.bt_hae);
        bt_paivita = findViewById(R.id.bt_paivita);

        // lisää-painikkeen toiminnallisuus
        tallenna.setOnClickListener(new View.OnClickListener() {

            @Override
            //metodi jolla luodaan tuote olio ja lisätään tiedot tuote listaan, kun tallenna nappia painetaan.
            public void onClick(View v) {


                //Tarkistetaan, ettei EAN koodi puutu
                if (TextUtils.isEmpty(textAnnaEan.getText())) {
                    Toast.makeText(getApplicationContext(), "EAN virheellinen", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Tarkistetaan, ettei nimi puutu.
                if (TextUtils.isEmpty(textAnnaTuoteNimi.getText())) {
                    Toast.makeText(getApplicationContext(), "Tuotteen nimi puuttuu", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Tarkistetaan ettei hinta puutu.
                if (TextUtils.isEmpty(textAnnaHinta.getText())) {
                    Toast.makeText(getApplicationContext(), "Hinta puuttuu", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TÄHÄN VARMENNUS JOKA TARKISTAA LÖYTYYKÖ EAN KOODI JO TIEDOSTOSTA...


                //Luodaan uusi tuote olio, joka saa arvokseen ean, nimen ja hinnan.
                Tuote tuote = new Tuote(Integer.parseInt(textAnnaEan.getText().toString()),
                        textAnnaTuoteNimi.getText().toString(),
                        Float.parseFloat(textAnnaHinta.getText().toString()));
                //Lisätään listaan
                tuoteArrayList.add(tuote);

                // Tallennetaan tiedot tiedostoon tuotteet.csv.
                try {
                    FileOutputStream fos = openFileOutput("tuotteet.csv", Context.MODE_PRIVATE);

                    for (Tuote t : tuoteArrayList) {
                        String line = t + "\n";
                        fos.write(line.getBytes());
                    }
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Tallentaminen epäonnistui", Toast.LENGTH_SHORT).show();
                }

                //Näytölle tuleva ilmoitus tallennuksen onnistumisesta
                Toast.makeText(getApplicationContext(), "TIEDOT TALLENNETTU", Toast.LENGTH_LONG).show();


                //Pyyhitään tekstikentät tyhjiksi automaattisesti, jos tallennus onnistui.
                textAnnaEan.setText("");
                textAnnaTuoteNimi.setText("");
                textAnnaHinta.setText("");
            }
        });

        // Hae-painikkeen toiminnallisuus
        bt_hae.setOnClickListener(new View.OnClickListener() {
            @Override
            //metodi jolla haetaan  olio
            public void onClick(View v) {
                //haetaan syötetty EAN-koodi
                String ean = textAnnaEan.getText().toString();

                //etsitään tuoteArrayListasta tuote, jolla on annettu EAN-koodi
                for (Tuote t : tuoteArrayList) {
                    if (t.getId() == Integer.parseInt(ean)) {
                        //jos tuote löytyy, täytetään sen tiedot tekstikenttiin
                        textAnnaTuoteNimi.setText(t.getNimi());
                        textAnnaHinta.setText(String.valueOf(t.getHinta()));
                        return;
                    }
                }

                //jos tuotetta ei löydy, annetaan käyttäjälle ilmoitus
                Toast.makeText(getApplicationContext(), "Tuotetta ei löytynyt", Toast.LENGTH_SHORT).show();
            }
        });

        // Poista-painikkeen toiminnallisuus
        bt_poista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //haetaan syötetty EAN-koodi
                String ean = textAnnaEan.getText().toString();

                //etsitään tuoteArrayLististasta tuotetta
                for (Tuote t : tuoteArrayList) {
                    if (t.getId() == Integer.parseInt(ean)) {
                        //kysytään käyttäjältä varmistus tuotteen poistamisesta
                        AlertDialog.Builder builder = new AlertDialog.Builder(lisaaUusiTuoteActivity.this);
                        builder.setMessage("Haluatko varmasti poistaa tuotteen?")
                                .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //poistetaan tuote tuoteArrayListasta
                                        tuoteArrayList.remove(t);
                                        //päivitetään tiedot tiedostoon
                                        try {
                                            FileOutputStream fos = openFileOutput("tuotteet.csv", Context.MODE_PRIVATE);
                                            for (Tuote t : tuoteArrayList) {
                                                String line = t + "\n";
                                                fos.write(line.getBytes());
                                            }
                                            fos.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Tallentaminen epäonnistui", Toast.LENGTH_SHORT).show();
                                        }
                                        //tyhjennetään kentät
                                        textAnnaEan.setText("");
                                        textAnnaTuoteNimi.setText("");
                                        textAnnaHinta.setText("");
                                        //annetaan käyttäjälle ilmoitus poistamisen onnistumisesta
                                        Toast.makeText(getApplicationContext(), "Tuote poistettu", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Peruuta", null);
                        AlertDialog alert = builder.create();
                        alert.show();
                        return;
                    }
                }

            }
        });

        // Päivitä-painikkeen toiminnallisuus
        bt_paivita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //haetaan syötetty EAN-koodi
                String ean = textAnnaEan.getText().toString();

                //etsitään tuoteArrayListasta tuote
                for (Tuote t : tuoteArrayList) {
                    if (t.getId() == Integer.parseInt(ean)) {
                        //päivitetään olion tiedot tekstikenttien mukaisesti
                        t.setNimi(textAnnaTuoteNimi.getText().toString());
                        t.setHinta(Float.parseFloat(textAnnaHinta.getText().toString()));

                        //tallennetaan päivitetty lista tiedostoon
                        tallennaTiedot();

                        //tyhjennetään EAN, nimi ja hinta -kentät
                        textAnnaEan.setText("");
                        textAnnaTuoteNimi.setText("");
                        textAnnaHinta.setText("");



                        //annetaan käyttäjälle ilmoitus päivityksen onnistumisesta
                        Toast.makeText(getApplicationContext(), "Tiedot päivitetty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

        });

    }

    //metodi joka tallentaa tuoteArrayListin tiedot tiedostoon
    private void tallennaTiedot() {
        try {
            FileOutputStream fos = openFileOutput("tuotteet.csv", Context.MODE_PRIVATE);

            for (Tuote t : tuoteArrayList) {
                String line = t + "\n";
                fos.write(line.getBytes());
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Tallentaminen epäonnistui", Toast.LENGTH_SHORT).show();
        }
    }



    /*
    Metodi, joka palauttaa ohjelman muutToiminnot luokkaan, kun käyttäjä painaa ActionBarissa olevaa nuolta.
    Ei kysy käyttäjältä erillistä varmistusta.
     */
        @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){
            switch (item.getItemId()) {
                case android.R.id.home:
                    // Palaa muutToiminnot luokkaan
                    Intent intent = new Intent(lisaaUusiTuoteActivity.this, muutToiminnotActivity.class);
                    startActivity(intent);
            }
            return super.onOptionsItemSelected(item);
        }

        //Ladataan tiedostossa olevat tiedot listaan jotta niitä voidaan käsitellä.
        // Ei lataa vielä tiedostossa olevie tietoja koska ei löydä tiedostoa.


    private void lataaTiedot() {
        try {
            FileInputStream fis = openFileInput("tuotteet.csv");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");

                int id = Integer.parseInt(parts[0]);
                String nimi = parts[1];
                float hinta = Float.parseFloat(parts[2]);

                Tuote tuote = new Tuote(id, nimi, hinta);
                tuoteArrayList.add(tuote);
            }

            br.close();
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Tiedostoa ei löytynyt", Toast.LENGTH_SHORT).show();
            String[] line = Tuote.alkuData().split("\n");

            for (String l : line) {
                String[] parts = l.split(";");
                int id = Integer.parseInt(parts[0]);
                String nimi = parts[1];
                float hinta = Float.parseFloat(parts[2]);
                Tuote tuote = new Tuote(id, nimi, hinta);
                tuoteArrayList.add(tuote);
            }
            tallennaTiedot();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Tiedoston lukeminen epäonnistui", Toast.LENGTH_SHORT).show();
        }
    }


    }




