package com.bonuscarnisapp;

import android.content.Intent;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muut_toiminnot);
        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Muita toimintoja");


        // LisaaUusituote -painikkeen toiminnallisuus
        buttonLisaa = findViewById(R.id.btLisaaUusiTuote);
        buttonLisaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaLisaaUusiTuote();
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

}