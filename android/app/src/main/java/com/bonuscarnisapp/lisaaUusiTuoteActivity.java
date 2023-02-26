package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
            //tästä tehty editable Tuote.javaan...
            //metodi jolla luodaan tuote olio ja lisätään tiedot tuote listaan kun tallenna nappia painetaan.
            public void onClick(View v) {
                Tuote tuote = new Tuote(textAnnaEan.getText(), textAnnaTuoteNimi.getText(), textAnnaHinta.getText());
                tuoteArrayList.add(tuote);
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