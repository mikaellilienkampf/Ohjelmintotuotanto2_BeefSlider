package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class eranSkannausActivity extends AppCompatActivity {

    private Button buttonAloitaSkannaus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eran_skannaus);

        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Otetaan syötetty nimi talteen ja päivitetään erän nimi ActionBariin
        String eranNimi = getIntent().getExtras().getString("eranNimiAvain");
        if (eranNimi.length() != 0) {
            actionBar.setTitle("Erä: " + eranNimi);
        } else {
            actionBar.setTitle("Nimetön erä");
        }

        // buttonAloitaSkannaus -painikkeen toiminnallisuus
        buttonAloitaSkannaus = findViewById(R.id.btAloitaSkannaus);
        buttonAloitaSkannaus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aloitaSkannaus(buttonAloitaSkannaus);
            }
        });
    }

    /*
    Metodi, joka määrittää mitä tapahtuu, kun käyttäjä klikkaa btAloitaSkannaus-painiketta
     */
    public void aloitaSkannaus(Button buttonAloitaSkannaus){
        // Piilotetaan kyseinen btAloitaSkannaus-painike
        buttonAloitaSkannaus.setVisibility(View.GONE);
        // Johonkin näkyviin ilmestyy "Lopeta skannaus" tms. painike, jolla erän skannauksen voi hallitusti lopettaa?
        // TODO
        // Kutsutaan viivakoodin-lukemisen hoitavaa metodia, jotta viivakoodinluku aktivoituu
        // TODO
    }

    /*
    Metodi, joka palauttaa ohjelman päävalikkoon, kun käyttäjä painaa ActionBarissa olevaa nuolta.
    Kysyy käyttäjältä varmistuksen.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Otetaan syötetty nimi talteen
        String eranNimi = getIntent().getExtras().getString("eranNimiAvain");

        switch (item.getItemId()) {
            case android.R.id.home:
                // Varmistetaan, että käyttäjä todella haluaa keskeyttää skannauksen ja palata alkuvalikkoon
                AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                builder.setTitle("Lopetetaanko skannaus?");
                if (eranNimi.length() == 0) {
                    builder.setMessage("Oletko varma, että haluat keskeyttää erän skannauksen ja poistua alkuvalikkoon?");
                } else {
                    builder.setMessage("Oletko varma, että haluat keskeyttää erän '" + eranNimi + "' skannauksen ja poistua alkuvalikkoon?");
                }

                // Jos käyttäjä klikkaa 'Kyllä'
                builder.setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Palaa alkuvalikkoon
                        Intent intent = new Intent(eranSkannausActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                // Jos käyttäjä klikkaa 'Ei'
                builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Sulje kysely ja jatka normaalisti
                        dialog.dismiss();
                    }
                });
                // Varmistus-dialogi näkyviin
                AlertDialog alert = builder.create();
                alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

}