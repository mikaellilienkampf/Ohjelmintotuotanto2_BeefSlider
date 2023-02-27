package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class lisaaUusiTuoteActivity extends AppCompatActivity {

    private Button tallenna;
    private EditText textAnnaEan;
    private EditText textAnnaTuoteNimi;
    private EditText textAnnaHinta;

    static ArrayList<Tuote> tuoteArrayList = new ArrayList<Tuote>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lisaa_uusi_tuote);

        // Paluu-painike Actionbariin
        ActionBar actionBar = getSupportActionBar();
        // ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Lisää uusi tuote");

        // Nimi-muuttujat talteen
        textAnnaEan = findViewById(R.id.textAnnaEan);

        textAnnaTuoteNimi = findViewById(R.id.textAnnaTuoteNimi);

        textAnnaHinta = findViewById(R.id.textAnnaHinta);

        // Tallenna-painikkeen toiminnallisuus
        tallenna = findViewById(R.id.tallennaButton);
        tallenna.setOnClickListener(new View.OnClickListener() {

            @Override
            //Tuote oliosta tehty editable Tuote.javaan...
            //metodi jolla luodaan tuote olio ja lisätään tiedot tuote listaan, kun tallenna nappia painetaan.
            public void onClick(View v) {

                /*
                //Tarkistetaan että EAN on 13 merkkiä pitkä.
                if (textAnnaEan.getText().length()!= 13) {
                    Toast.makeText(getApplicationContext(), "EAN virheellinen", Toast.LENGTH_SHORT).show();
                    return;
                }
                */

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

                //Luodaan uusi tuote olio, joka saa arvokseen ean, nimen ja hinnan.
                Tuote tuote = new Tuote(textAnnaEan.getText(), textAnnaTuoteNimi.getText(), textAnnaHinta.getText());
                //Lisätään listaan
                tuoteArrayList.add(tuote);

                //Tiedosto kansio pitää olla luotu ensin eli nyt tiedosto puuttuu.
                // Tallennetaan tiedot tiedostoon tuotteet.csv. HUOM. Tuote oliosta tehty editable Tuote.javaan.
                //pitää muuttaa tyyppiä jos ei toimi String line = t.getId() + "," + t.getNimi() + "," + t.getHinta()
                try {
                    FileOutputStream fos = openFileOutput("tuotteet.csv", Context.MODE_PRIVATE);

                    for (Tuote t : tuoteArrayList) {
                        String line = t.getId() + "," + t.getNimi() + "," + t.getHinta() + "\n";
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

    }

    /*
    Metodi, joka palauttaa ohjelman muutToiminnot luokkaan, kun käyttäjä painaa ActionBarissa olevaa nuolta.
    Ei kysy käyttäjältä erillistä varmistusta.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Palaa muutToiminnot luokkaan
                Intent intent = new Intent(lisaaUusiTuoteActivity.this, muutToiminnotActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}