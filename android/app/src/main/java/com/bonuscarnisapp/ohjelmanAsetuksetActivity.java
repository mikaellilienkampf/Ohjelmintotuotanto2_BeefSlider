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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;


public class ohjelmanAsetuksetActivity extends AppCompatActivity implements View.OnClickListener {

    // tvAsetettuSahkopostiosoite
    private TextView textViewAsetettuSahkopostiosoite;
    // ibMuokkaaAsetettuaSahkopostiosoitetta
    private ImageButton imageButtonMuokkaaAsetettuaSahkopostiosoitetta;
    private Switch switchTeema;
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;
    private Button buttonSahkoposti;
    @Override
    public void onClick(View view) {
    }
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
        actionBar.setTitle("");

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
        textViewAsetettuSahkopostiosoite.setTextSize(20);
        if(oletussahkoposti == null || oletussahkoposti.length() == 0){
            textViewAsetettuSahkopostiosoite.setTextSize(13);
            textViewAsetettuSahkopostiosoite.setTextColor(Color.RED);
            textViewAsetettuSahkopostiosoite.setText("Sähköpostiosoitetta ei ole vielä asetettu!");
        } else {
            textViewAsetettuSahkopostiosoite.setTextSize(20);
            textViewAsetettuSahkopostiosoite.setTextColor(Color.BLACK);
            textViewAsetettuSahkopostiosoite.setText(oletussahkoposti);
        }

        // Asetetun sähköpostiosoitteen muokkaus (ei ainakaan vielä tarkista syötettyä sähköpostiosoitetta mitenkään)
        imageButtonMuokkaaAsetettuaSahkopostiosoitetta = findViewById(R.id.ibMuokkaaAsetettuaSahkopostiosoitetta);
        imageButtonMuokkaaAsetettuaSahkopostiosoitetta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("Aseta uusi oletussähköpostiosoite tai muokkaa nykyisen salasanaa.");

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
                        if(input.getText().toString().length() > 0) {
                            textViewAsetettuSahkopostiosoite.setText(input.getText().toString());
                            textViewAsetettuSahkopostiosoite.setTextSize(20);
                            textViewAsetettuSahkopostiosoite.setTextColor(Color.BLACK); // Varmistetaan, että tekstin väri vaihtuu
                        } else {
                            textViewAsetettuSahkopostiosoite.setTextSize(13);
                            textViewAsetettuSahkopostiosoite.setTextColor(Color.RED);
                            textViewAsetettuSahkopostiosoite.setText("Sähköpostiosoitetta ei ole vielä asetettu!");
                        }
                    }
                });
                // Toiminnallisuus, kun käyttäjä klikkaa "Peruuta"
                alert.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Peru sähköpostiosoitteen muokkaus
                    }
                });
                // Salasana
                alert.setNeutralButton("Salasana", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        AlertDialog.Builder alert2 = new AlertDialog.Builder(v.getContext());
                        alert2.setTitle("Aseta salasana");

                        // EditText, johon salasana kirjoitetaan
                        final EditText input2 = new EditText(v.getContext());
                        input2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // Olettaa syötteen olevan salasana
                        alert2.setView(input2);

                        // Toiminnallisuus, kun käyttäjä klikkaa "Vahvista"
                        alert2.setPositiveButton("Vahvista", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Testataan salasanan pituus (tulisi olla Gmailissa 16 merkkiä
                                if(input2.getText().toString().length() != 16){
                                    AlertDialog.Builder alert3 = new AlertDialog.Builder(v.getContext());
                                    alert3.setTitle("Oletko varma, että salasana on oikein?");
                                    alert3.setMessage("Gmailin salasanan pitäisi olla 16-merkkinen");
                                    // Toiminnallisuus, kun käyttäjä klikkaa "Vahvista"
                                    alert3.setPositiveButton("Vahvista", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Tallennetaan uusi salasana SharedPreferensseihin
                                            // Testauksen ja kehityksen ajan pois toiminnasta!
                                            //SharedPreferences.Editor editor2 = sharedPref.edit();
                                            //editor2.putString("defaultEmailPassword", input2.getText().toString());
                                            //editor2.commit();
                                        }
                                    });
                                    // Toiminnallisuus, kun käyttäjä klikkaa "Peruuta"
                                    alert3.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Peru salasanan muokkaus
                                        }
                                    });
                                    alert3.show();
                                }
                                // Tallennetaan uusi salasana SharedPreferensseihin
                                SharedPreferences.Editor editor3 = sharedPref.edit();
                                editor3.putString("defaultEmailPassword", input2.getText().toString());
                                editor3.commit();
                            }
                        });
                        // Toiminnallisuus, kun käyttäjä klikkaa "Peruuta"
                        alert2.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Peru sähköpostiosoitteen muokkaus
                            }
                        });
                        alert2.show(); // Alert2 näkyviin
                    }
                });
                alert.show(); // Alert näkyviin
            }
        });

        // sähköpostipainike
        buttonSahkoposti = findViewById(R.id.btTestaaSahkopostinToimivuus);
        buttonSahkoposti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Luodaan Sahkopostiviesti-olio (parametreina context, viestin aihe ja sisältö)
                String[] viestinLiitteet = {};
                Sahkopostiviesti sahkopostiviesti = new Sahkopostiviesti(getApplicationContext(), "Testiviesti", "Tämä on sovelluksen lähettämä testiviesti. Ei lähetä enää liitteitä.", viestinLiitteet);
                // Lähetetään sähköpostiviesti (parametrina view)
                sahkopostiviesti.lahetaSahkopostiviesti(v);
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