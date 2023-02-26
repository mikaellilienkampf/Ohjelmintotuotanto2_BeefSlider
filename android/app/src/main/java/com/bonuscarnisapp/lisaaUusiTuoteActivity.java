package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class lisaaUusiTuoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lisaa_uusi_tuote);

        // Paluu-painike Actionbariin
        ActionBar actionBar = getSupportActionBar();
        // ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Lisää uusi tuote");

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