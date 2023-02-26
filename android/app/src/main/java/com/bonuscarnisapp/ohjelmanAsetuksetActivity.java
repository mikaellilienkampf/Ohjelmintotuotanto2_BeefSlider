package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import android.widget.CompoundButton;
import android.widget.Switch;



public class ohjelmanAsetuksetActivity extends AppCompatActivity {

    private Switch switchTeema;
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
        setContentView(R.layout.activity_ohjelman_asetukset);

        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Päivitetään otsikko
        actionBar.setTitle("Asetukset");

        switchTeema = findViewById(R.id.switchTeema);

        // Aseta switchin tila tallennetun teeman mukaiseksi
        switchTeema.setChecked(isDarkTheme);

        switchTeema.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Tallenna teeman tila SharedPreferencesiin
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("darkTheme", isChecked);
                editor.apply();

                // Käynnistä uudelleen tämä activity uudella teemalla
                Intent intent = getIntent();
                finish();
                startActivity(intent);
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
                Intent intent = new Intent(ohjelmanAsetuksetActivity.this, MainActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}