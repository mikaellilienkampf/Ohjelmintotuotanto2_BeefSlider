package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class eranNimeaminenActivity extends AppCompatActivity {

    // Alustetaan painikkeet ja kentät
    private Button buttonPeruuta;
    private Button buttonJatka;
    private TextView editTextAnnaNimi;
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
        setContentView(R.layout.activity_eran_nimeaminen);

        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Päivitetään otsikko
        actionBar.setTitle("Erän nimeäminen");

        // Nimi-muuttuja talteen
        editTextAnnaNimi = findViewById(R.id.textAnnaNimi);

        // Peruuta-painikkeen toiminnallisuus
        buttonPeruuta = findViewById(R.id.btPeruuta);
        buttonPeruuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(eranNimeaminenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        /// Jatka-painikkeen toiminnallisuus
        buttonJatka = findViewById(R.id.btJatka);
        buttonJatka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(eranNimeaminenActivity.this, eranSkannausActivity.class);
                intent.putExtra("eranNimiAvain", editTextAnnaNimi.getText().toString() );
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
                Intent intent = new Intent(eranNimeaminenActivity.this, MainActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}