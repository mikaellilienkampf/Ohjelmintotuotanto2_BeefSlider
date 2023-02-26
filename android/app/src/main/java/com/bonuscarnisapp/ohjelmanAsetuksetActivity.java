package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.MenuItem;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;


public class ohjelmanAsetuksetActivity extends AppCompatActivity {

    // tvAsetettuSahkopostiosoite
    private TextView textViewAsetettuSahkopostiosoite;
    // ibMuokkaaAsetettuaSahkopostiosoitetta
    private ImageButton imageButtonMuokkaaAsetettuaSahkopostiosoitetta;
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
        actionBar.setTitle("... palaa alkuvalikkoon");

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

        // Haetaan aiemmin asetettu oletussähköposti tvAsetettuSahkopostiosoite-textViewiin
        String oletussahkoposti = sharedPref.getString("defaultEmail", null);
        textViewAsetettuSahkopostiosoite = findViewById(R.id.tvAsetettuSahkopostiosoite);
        if(oletussahkoposti == null){
            textViewAsetettuSahkopostiosoite.setTextSize(14);
            textViewAsetettuSahkopostiosoite.setTextColor(Color.RED);
            textViewAsetettuSahkopostiosoite.setText("Sähköpostiosoitetta ei ole vielä asetettu!");
        } else {
            textViewAsetettuSahkopostiosoite.setText(oletussahkoposti);
        }

        // Asetetun sähköpostiosoitteen muokkaus (ei ainakaan vielä tarkista syötettyä sähköpostiosoitetta mitenkään)
        imageButtonMuokkaaAsetettuaSahkopostiosoitetta = findViewById(R.id.ibMuokkaaAsetettuaSahkopostiosoitetta);
        imageButtonMuokkaaAsetettuaSahkopostiosoitetta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("Aseta uusi oletussähköpostiosoite");

                // EditText, johon uusi sähköpostiosoite kirjoitetaan
                final EditText input = new EditText(v.getContext());
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); // Olettaa syötteen olevan sähköpostiosoite
                alert.setView(input);
                // Toiminnallisuus, kun käyttäjä klikkaa "Vahvista"
                alert.setPositiveButton("Vahvista", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Tallennetaan uusi osoite SharedPreferensseihin
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("defaultEmail", input.getText().toString());
                        editor.commit();
                        // Asetetaan myös tvAsetettuSahkopostiosoite-textViewiin
                        textViewAsetettuSahkopostiosoite.setText(input.getText().toString());
                        textViewAsetettuSahkopostiosoite.setTextColor(Color.BLACK); // Varmistetaan, että tekstin väri vaihtuu
                    }
                });
                // Toiminnallisuus, kun käyttäjä klikkaa "Peruuta"
                alert.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Peru sähköpostiosoitteen muokkaus
                    }
                });
                alert.show(); // Alert näkyviin
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