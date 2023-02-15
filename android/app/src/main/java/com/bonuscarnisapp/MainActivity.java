package com.bonuscarnisapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    // Alustetaan aloitusnäkymän painikkeet
    private Button buttonAloitaUudenEranSkannaus;
    private Button buttonSelaaJaMuokkaa;
    private Button buttonAvaaMuutToiminnot;
    private ImageButton imageButtonAloitussivunValikko;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // AloitussivunValikko -painikkeen toiminnallisuus
        imageButtonAloitussivunValikko = findViewById(R.id.ibAloitussivunValikko);
        imageButtonAloitussivunValikko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avaaAloitussivunValikko(v);
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

    /*
     Metodi, joka määrittää mitä tapahtuu, kun käyttäjä painaa ibAloitussivunValikko-painiketta
     */
    public void avaaAloitussivunValikko(View v){
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.aloitussivunvalikko);
        popup.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, ohjelmanAsetuksetActivity.class);
                startActivity(intent);
                return true;
            case R.id.exit:

                // Varmistetaan, että käyttäjä todella haluaa sulkea ohjelman
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Lopetetaanko ohjelma?");
                builder.setMessage("Oletko varma, että haluat sulkea ohjelman?");
                builder.setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Sulje ohjelma
                        finish();
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Sulje ikkuna ja jatka normaalisti
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}