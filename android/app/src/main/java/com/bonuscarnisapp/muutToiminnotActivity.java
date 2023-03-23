package com.bonuscarnisapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class muutToiminnotActivity extends AppCompatActivity {

    private Button buttonLisaa;
    private Button buttonAsetukset;
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;

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
        setContentView(R.layout.activity_muut_toiminnot);
        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Muita toimintoja");


        // Tuotelista muokkaus -painikkeen toiminnallisuus
        buttonLisaa = findViewById(R.id.btLisaaUusiTuote);
        buttonLisaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaLisaaUusiTuote();
            }
        });

        // Asetukset -painikkeen toiminnallisuus
        buttonAsetukset = findViewById(R.id.btAsetukset);
        buttonAsetukset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaAsetukset();
            }
        });



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
                Intent intent = new Intent(muutToiminnotActivity.this, MainActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     Metodi, joka määrittää mitä tapahtuu, kun käyttäjä painaa btLisaauusiTuote-painiketta
     */
    public void avaaLisaaUusiTuote(){
        Intent intent = new Intent(this, lisaaUusiTuoteActivity.class);
        startActivity(intent);
    }

    public void avaaAsetukset() {
        Intent intent = new Intent(this, ohjelmanAsetuksetActivity.class);
        startActivity(intent);
    }
}