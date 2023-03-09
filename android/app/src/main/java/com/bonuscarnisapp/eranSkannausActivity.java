package com.bonuscarnisapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.UUID;

public class eranSkannausActivity extends AppCompatActivity {

    private Button buttonAloitaSkannaus;

    String firstTwoChars;
    String nextSixChars;
    String nextFourChars;
    String lastChar;
    int skannattujenmaara;
    Button listaanappi;
    int kokopaino;

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


        listaanappi =findViewById(R.id.btListaaTuote);


        listaanappi.setOnClickListener(v->
        {

            skannattujenmaara = skannattujenmaara + 1;
            kokopaino = kokopaino + Integer.parseInt(nextFourChars);
            TextView txtView2 = (TextView)findViewById(R.id.tvYhteenvetoSkannauksista);
            txtView2.setText("Tuotteita yht: " + skannattujenmaara + " - Tuotteitten paino: " + kokopaino + " grammaa");
            buttonAloitaSkannaus.setVisibility(View.VISIBLE);
            listaanappi.setVisibility(View.GONE);
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
        scanCode();
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

    private void scanCode() {
        // Asetetaan asetukset viivakoodinlukijalle.
        ScanOptions options = new ScanOptions();
        options.setPrompt("---");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);

        // Luodaan uusi launcher viivakoodinlukijalle ja käynnistetään se annetuilla asetuksilla.
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            // Luodaan uusi hälytysikkuna näyttämään viivakoodinlukijan tulos. (testaustarkoitukseen, väliaikainen)
            AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
            builder.setTitle("Tulos");
            builder.setMessage(result.getContents());

            TextView txtView = (TextView)findViewById(R.id.tvSkannattuTuote);
            splitString(result.getContents());
            txtView.setText("Tuotteen nimi: " + nextSixChars + " - Tuotteen paino: " + nextFourChars + " grammaa");
            listaanappi.setVisibility(View.VISIBLE);


            // Lisätään OK button hälytysikkunaan ja asetetaan sille klikkaustapahtuman kuuntelija, joka sulkee ikkunan, kun sitä painetaan.
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    // jakaa barcoden eri osat muuttujiin
    public void splitString(String x) {
        //firstTwoChars = x.substring(0, 2);
        nextSixChars = x.substring(2, 8);
        nextFourChars = x.substring(8, 12);
        //lastChar = x.substring(12, 13);


    }

    // tarkistaa että koodi on oikeanlainen
    public static boolean tarkistakoodi(String y) {
        if (y == null || y.length() != 13) {
            return false; // y on null tai ei ole 13 merkkiä pitkä
        }

        // tarkista, että y sisältää vain numeroita
        for (char c : y.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false; // y sisältää muita kuin numeroita
            }
        }

        // tarkista, että kahdella ensimmäisellä merkillä on arvot "2" ja "9"
        if (y.charAt(0) != '2' || y.charAt(1) != '8') {
            return false; // kahdella ensimmäisellä merkillä ei ole arvoja "2" ja "8"
        }

        return true; // y on kelvollinen
    }

    // generoi random id:n
    public static String generateRandomID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}