package com.bonuscarnisapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

public class MainActivity extends AppCompatActivity {

    // Alustetaan aloitusnäkymän painikkeet
    private Button buttonAloitaUudenEranSkannaus;
    private Button buttonSelaaJaMuokkaa;
    private Button buttonAvaaMuutToiminnot;

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
        setContentView(R.layout.activity_main);


        // ActionBarin otsikon päivitys
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Alkuvalikko");

        // aloitaUudenEranSkannaus -painikkeen toiminnallisuus
        buttonAloitaUudenEranSkannaus = findViewById(R.id.btAloitaUudenEranSkannaus);
        buttonAloitaUudenEranSkannaus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaEranNimeaminen();
            }
        });

        // selaaJaMuokkaa -painikkeen toiminnallisuus
        buttonSelaaJaMuokkaa = findViewById(R.id.btSelaaJaMuokkaa);
        buttonSelaaJaMuokkaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaSelaustila();
            }
        });

        // avaaMuutToiminnot -painikkeen toiminnallisuus
        buttonAvaaMuutToiminnot = findViewById(R.id.btMuutToiminnot);
        buttonAvaaMuutToiminnot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaMuutToiminnotTila();
            }
        });
    }


    /*
     Metodi, joka määrittää mitä tapahtuu, kun käyttäjä painaa btAloitaUudenEranSkannaus-painiketta
     */
    public void avaaEranNimeaminen(){
        Intent intent = new Intent(this, eranNimeaminenActivity.class);
        startActivity(intent);
    }

    /*
     Metodi, joka määrittää mitä tapahtuu, kun käyttäjä painaa btSelaaJaMuokkaa-painiketta
     */
    public void avaaSelaustila(){
        Intent intent = new Intent(this, erienSelausActivity.class);
        startActivity(intent);
    }

    /*
     Metodi, joka määrittää mitä tapahtuu, kun käyttäjä painaa btMuutToiminnot-painiketta
     */
    public void avaaMuutToiminnotTila(){
        Intent intent = new Intent(this, muutToiminnotActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onBackPressed() {
        // Suorita sulku-toimet
        finishAffinity(); // Sulkee sovelluksen ja kaikki sen aktiiviset prosessit
    }


}