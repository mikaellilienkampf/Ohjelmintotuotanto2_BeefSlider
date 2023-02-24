package com.bonuscarnisapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.MenuItem;

import android.widget.CompoundButton;
import android.widget.Switch;



public class ohjelmanAsetuksetActivity extends AppCompatActivity {

    private Switch switchTeema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ohjelman_asetukset);

        // Paluu-painike Actionbariin...
        ActionBar actionBar = getSupportActionBar();
        // ...ActionBarin Paluu-painike näkyviin
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Päivitetään otsikko
        actionBar.setTitle("Asetukset");

        //Tarkistetaan, onko teema jo asetettu
        SharedPreferences prefs = getSharedPreferences("THEME", MODE_PRIVATE);
        int themeId = prefs.getInt("theme", R.style.LightTheme);
        setTheme(themeId);
        // Night mode switch
        switchTeema = findViewById(R.id.switchTeema);
        //Asettaa switchin tiettyyn tilaan teeman mukaan
        if (themeId == R.style.LightTheme) {
            switchTeema.setChecked(false);
        } else {
            switchTeema.setChecked(true);
        }
        switchTeema.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int themeId;
                if(isChecked){
                    themeId = R.style.DarkTheme;
                } else {
                    themeId = R.style.LightTheme;
                }
                changeTheme(themeId);

            }
            private void changeTheme(int themeId) {
                SharedPreferences.Editor editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                editor.putInt("theme", themeId);
                editor.apply();
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