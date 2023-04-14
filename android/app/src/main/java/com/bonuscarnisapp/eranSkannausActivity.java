package com.bonuscarnisapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class eranSkannausActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;
    private Button buttonAktivoiSkanneri;
    private Button buttonEraValmis;
    private Button buttonLahetaSahkopostiin;
    private boolean jatkettavaEra;
    private String erotinmerkki = ",";
    private ArrayAdapter<String> arr;
    private TextView textvievYhteenvetoSkannauksista;
    private ListView listviewListatutTuotteet;
    private ArrayList<String> listatutTuotteet;
    private static Context context;
    private String eranNimi;
    private String tiedostonimi;
    private String tuotteenNimi;
    private int tuotteenPainoGrammoina;
    private String tuotteenKilohinta;
    private Double tuotteenArvoEuroissa;
    private HashMap<String, String[]> tuotteidenTiedot;
    private HashMap<String, ArrayList<String>> skannatutTuotteet;
    String firstTwoChars;
    String nextSixChars;
    String nextFourChars;
    String lastChar;
    int skannattujenmaara;
    int poistettujenMaara = 0;
    private static final DecimalFormat dfEuro = new DecimalFormat("0.00");
    private static final DecimalFormat dfGramma = new DecimalFormat("0");
    int kokopaino;
    double yhteishinta = 0.00;
    String jatkettavanEranTiedostonimi;

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

        // Alustetaan kontekstimuuttuja
        context = getApplicationContext();

        // Textview, johon yhteenveto skannatuista tuotteista
        textvievYhteenvetoSkannauksista = findViewById(R.id.tvYhteenvetoSkannauksista);

        // Alustetaan skannattujen tuotteiden määrä nollaksi
        skannattujenmaara = 0;

        // Luetaan tuotteet.csv tiedoston sisältö muistiin: jos tiedostoa ei löydy, kehoitetaan käyttäjää käymään alustamassa tiedosto.
        tuotteidenTiedot = new HashMap<String, String[]>(); // Tallennetaan tiedot HashMappiin
        String line = "";
        String splitBy = ";"; // Tuotteet.csv tiedostossa näkyy olevan erottimena ";"-merkki.

        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(context.getFilesDir(), "tuotteet.csv")));
            // Käydään läpi tiedoston rivit
            while((line = br.readLine()) != null) {
                String[] tuote = line.split(splitBy); // Tallennetaan rivin tiedot
                String[] luetunTuotteenTiedot = new String[2]; // Alustetaan nimelle ja kilohinnalle kaksipaikkainen array
                luetunTuotteenTiedot[0] = tuote[1]; // Tallennetaan tuotteen nimi
                luetunTuotteenTiedot[1] = tuote[2].replace(",", "."); // Tallennetaan tuotteen kilohinta (huom. desimaalierottimena piste!)
                tuotteidenTiedot.put(tuote[0], luetunTuotteenTiedot); // Tallennetaan tiedot hashMappiin
            }
        } catch (IOException e) {
            // Luodaan uusi hälytysikkuna kertomaan, että tuotteet.csv tiedostoa ei ole vielä alustettu
            AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
            builder.setTitle("Virhe!");
            builder.setMessage("Tuotteiden tiedot sisältävää tiedostoa ei ole vielä alustettu! Alustus tapahtuu käymällä Tuotelistan muokkaus -tilassa.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Sulje kysely ja jatka normaalisti
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        // buttonEraValmis -painikkeen toiminnallisuus
        buttonEraValmis =findViewById(R.id.btValmis);
        buttonEraValmis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kuittaaEraValmiiksi();
            }
        });

        // buttonLahetaSahkopostiin -painikkeen toiminnallisuus
        buttonLahetaSahkopostiin = findViewById(R.id.btLahetaSahkopostiin);
        buttonLahetaSahkopostiin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lahetaSahkopostiin();
            }
        });

        // buttonAloitaSkannaus -painikkeen toiminnallisuus
        buttonAktivoiSkanneri = findViewById(R.id.btAktivoiSkanneri);
        buttonAktivoiSkanneri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aloitaSkannaus();
            }
        });

        // Tarkistetaan, onko kyseessä uusi erä vai jatketaanko vanhan skannausta
        jatkettavaEra = false;
        jatkettavanEranTiedostonimi = getIntent().getExtras().getString("eranTiedostonimiAvain");
        // Jos eranTiedostonimiAvain ei palauta nullia (vaan tiedostonimen) on kyseessä jatkettava erä
        if(jatkettavanEranTiedostonimi != null){
            jatkettavaEra = true;
        }


        // Jos kyseessä on uusi erä:
        if(jatkettavaEra == false) {
            // Otetaan syötetty nimi talteen ja päivitetään erän nimi ActionBariin
            eranNimi = getIntent().getExtras().getString("eranNimiAvain");
            if (eranNimi.length() != 0) {
                actionBar.setTitle("Uusi erä: " + eranNimi);
            } else {
                actionBar.setTitle("Uusi nimetön erä");
            }
            // Alustetaan listatut tuotteet (tyhjä arraylist)
            listatutTuotteet = new ArrayList<String>();
            // Alustetaan tyhjä HashMap, jolla pidetään kirjaa skannatuista tuotteista
            skannatutTuotteet = new HashMap<String, ArrayList<String>>(); // Tallennetaan tiedot HashMappiin

        // Jos kyseessä on aiemmin luotu erä, jota jatketaan
        } else {
            // Päivitetään erän nimi actionbariin
            actionBar.setTitle("Jatketaan: " + jatkettavanEranTiedostonimi);

            // Haetaan skannatut tuotteet tiedostosta
            skannatutTuotteet = haeAiemmatSkannaukset(jatkettavanEranTiedostonimi);
            //System.out.println("SIK:" + skannatutTuotteet.size());
            if(skannatutTuotteet.size() > 0) {
                listatutTuotteet = paivitaListatutTuotteet();
                if (listatutTuotteet.size() > 0) {
                    buttonLahetaSahkopostiin.setEnabled(true); // Mahdollistetaan sähköpostin lähetys
                    buttonEraValmis.setEnabled(true); // Mahdollistetaan erän valmistumisen merkkaaminen
                }
            } else {
                listatutTuotteet = new ArrayList<String>();
            }
        }
        // Luodaan arrayAdapter listviewille
        listviewListatutTuotteet = findViewById(R.id.lvListatutTuotteet);
        //listatutTuotteet = new ArrayList<>();
        arr = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listatutTuotteet);
        listviewListatutTuotteet.setAdapter(arr);
        listviewListatutTuotteet.setChoiceMode(listviewListatutTuotteet.CHOICE_MODE_SINGLE); // Voi valita monta itemiä
        listviewListatutTuotteet.setTranscriptMode(listviewListatutTuotteet.TRANSCRIPT_MODE_ALWAYS_SCROLL); // Scrollaa automaattisesti alas, jos tuotteita tulee paljon

        // Valittua tuoteryhmää kerran painamalla pääsee käsiksi erän sisältämään dataan.
        listviewListatutTuotteet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String valitunTuoteryhmanTuotekoodi = (String) listviewListatutTuotteet.getItemAtPosition( position );
                //System.out.println("Valittu tuote: " + valitunTuoteryhmanTuotekoodi);
                //Kutsutaan metodia, jolla pääsee käsiksi dataan ja poistamaan sitä. Parametrina kuusinumeroinen tuotekoodi
                katseleTuoteryhmanSkannauksia(valitunTuoteryhmanTuotekoodi.substring(0,6));
            }
        });
    }

    /*
    Metodi, joka täyttää listatutTuotteet arraylistin HashMapin perusteella
     */
    public ArrayList<String> paivitaListatutTuotteet(){
        // Alustetaan tyhjä arraylist, johon kerätään tiedot hashmapista
        ArrayList<String> palautettavaArray = new ArrayList<String>();
        // Loopataan HashMap läpi
        for (HashMap.Entry<String, ArrayList<String>> entry : skannatutTuotteet.entrySet()) {
            // Haetaan ko. tuotteen tuotenimi
            String tuotenimi = tuotteidenTiedot.get(entry.getKey())[0];
            // Haetaan ko. tuotteen arraylist
            ArrayList<String> tuotteenSkannaukset = skannatutTuotteet.get(entry.getKey());
            int tuotteidenMaara = tuotteenSkannaukset.size();
            // Jos tuotteita on olemassa (periaatteessa ne voidaan poistaa kaikki)

            // Alustetaan apumuuttujat
            int tuotteidenYhteispaino = 0;
            double tuotteidenYhteisarvo = 0.00;
            // Loopataan arraylistin sisältö läpi ja kasvatetaan painoa ja arvoa
            for (int i = 0; i < tuotteenSkannaukset.size(); i++) {
                String tuotteenTiedot = tuotteenSkannaukset.get(i);
                String[] tuotteenTiedotTaulukossa = tuotteenTiedot.split(" - ");
                tuotteidenYhteispaino = tuotteidenYhteispaino + Integer.parseInt(tuotteenTiedotTaulukossa[2].substring(0, tuotteenTiedotTaulukossa[2].length() - 2));
                tuotteidenYhteisarvo = tuotteidenYhteisarvo + Double.parseDouble(tuotteenTiedotTaulukossa[3].substring(0, tuotteenTiedotTaulukossa[3].length() - 2).replace(",", "."));
            }
            String uusiTuotteenYhteenveto = entry.getKey() + " - " + tuotenimi + " (" + tuotteidenMaara + " kpl) - " + tuotteidenYhteispaino + " g - " + dfEuro.format(tuotteidenYhteisarvo) + " €";
            //System.out.println("aiemmin skannatun yht: " + uusiTuotteenYhteenveto);
            palautettavaArray.add(uusiTuotteenYhteenveto);
        }
        //System.out.println("KOKO ON: " + palautettavaArray.size());
        return palautettavaArray;
    }

    /*
    Metodi, joka hakee aiemmin skannatun erän skannaukset tiedostosta ja tallentaa ne hashmappiin
     */
    public HashMap<String, ArrayList<String>> haeAiemmatSkannaukset(String jatkettavanTiedostonimi){
        String aiempiSkannaus;
        String aiemmanSkannauksenSplitBy = ",";
        String tiedostopolku = getFilesDir().getAbsolutePath() + File.separator + "csv_tiedostot";
        HashMap<String, ArrayList<String>> aiemmatSkannaukset = new HashMap<String, ArrayList<String>>();
        try{
            BufferedReader br2 = new BufferedReader(new FileReader(new File(tiedostopolku, jatkettavanTiedostonimi)));
            br2.readLine(); // Luetaan otsikkorivi pois
            // Käydään läpi tiedoston rivit
            while((aiempiSkannaus = br2.readLine()) != null) {

                String[] aiemminSkannattuTuote = aiempiSkannaus.split(aiemmanSkannauksenSplitBy); // Tallennetaan rivin tiedot
                String aiemminSkannatunTuotteenTuotekoodi = aiemminSkannattuTuote[0];
                String aiemminSkannatunTuotteenTuotenimi = aiemminSkannattuTuote[1];
                int aiemminSkannatunTuotteenPainoGrammoina = Integer.parseInt(aiemminSkannattuTuote[2]);
                double aiemminSKannatunTuotteenArvoEuroissa = Double.parseDouble(aiemminSkannattuTuote[4]);
                String aiempiSkannausTekstina = aiemminSkannatunTuotteenTuotekoodi + " - " + aiemminSkannatunTuotteenTuotenimi + " - " + dfGramma.format(aiemminSkannatunTuotteenPainoGrammoina) + " g" + " - " + dfEuro.format(aiemminSKannatunTuotteenArvoEuroissa) + " €";
                // Koostetaan tietoja
                skannattujenmaara = skannattujenmaara + 1;
                yhteishinta = yhteishinta + aiemminSKannatunTuotteenArvoEuroissa;
                kokopaino = kokopaino + aiemminSkannatunTuotteenPainoGrammoina;
                // Alustetaan tyhjä array, johon tiedot tallennetaaan
                ArrayList<String> aiemmatSkannauksetArray = new ArrayList<String>();
                // Jos tuotekoodi on jo hashMapissa
                if(aiemmatSkannaukset.containsKey(aiemminSkannatunTuotteenTuotekoodi)){
                    // Luetaan arraylist HashMapista
                    aiemmatSkannauksetArray = aiemmatSkannaukset.get(aiemminSkannatunTuotteenTuotekoodi);
                    // Lisätään juuri luetun rivin tiedot arraylistiin
                    aiemmatSkannauksetArray.add(aiempiSkannausTekstina);
                    // Päivitetään HashMap
                    aiemmatSkannaukset.put(aiemminSkannatunTuotteenTuotekoodi, aiemmatSkannauksetArray);
                } else {
                    aiemmatSkannauksetArray.add(aiempiSkannausTekstina);
                    // Jos tuotekoodia ei vielä ollut HashMapissa, lisätään hashmappiin ko. avain ja arraylist, jossa juuri luettu tieto
                    aiemmatSkannaukset.put(aiemminSkannatunTuotteenTuotekoodi, aiemmatSkannauksetArray);
                }
            }
            // Päivitetään kokonaisyhteenveto
            String paivitettyYhteenveto = "Skannattuja tuotteita " + skannattujenmaara + " kpl, yht. " + Double.toString(((double) kokopaino)/1000) + " kg, " + dfEuro.format(Math.abs(yhteishinta)) + " €";
            textvievYhteenvetoSkannauksista.setText(paivitettyYhteenveto);
        } catch (IOException e) {
            // Luodaan uusi hälytysikkuna kertomaan, että tuotteet.csv tiedostoa ei ole vielä alustettu
            AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
            builder.setTitle("Virhe!");
            builder.setMessage("Aiempien skannauksien lukeminen tiedostosta ei onnistunut!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Sulje kysely ja jatka normaalisti
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        // Palautetaan tieto
        return aiemmatSkannaukset;
    }

    /*
    Metodi, joka poistaa halatun tuoteryhmän listviewista
    */
    public void poistaTuoteryhmaListviewista(String tuotekoodi) {
        // Jos tuotteidenMaara meni nollaan, poistetaan koko homma listalta
            int poistettavanIndeksi = 0;
            // Loopataan listatutTuotteet lapi ja paivitetaan oikeasta kohdasta
            for (int j = 0; j < listatutTuotteet.size(); j++) {
                String tuotteenTuotekoodiYhteenvedosta = listatutTuotteet.get(j).substring(0, 6);
                if (tuotteenTuotekoodiYhteenvedosta.equals(tuotekoodi)) {
                    poistettavanIndeksi = j;
                    break;
                }
            }
            // Poistetaan indeksistä ja päivitetään adapteri
            listatutTuotteet.remove(poistettavanIndeksi);
            arr.notifyDataSetChanged();
    }

    /*
    Metodi, joka avaa ja listaa halutun tuoteryhmän skannaukset
     */
    public void katseleTuoteryhmanSkannauksia(String tuotekoodi){
        //System.out.println("Kutsuttiin tuotekoodilla: " + tuotekoodi);
        // Haetaan ko. tuotteen skannaukset HashMapista
        ArrayList<String> tuoteryhmanSkannaukset = skannatutTuotteet.get(tuotekoodi);

        AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
        builder.setTitle("Tuoteryhmän skannatut tuotteet");

        ListView dataListView = new ListView(eranSkannausActivity.this);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(eranSkannausActivity.this, android.R.layout.simple_list_item_1, tuoteryhmanSkannaukset);
        dataListView.setAdapter(dataAdapter);
        builder.setView(dataListView);
        builder.setPositiveButton("Palaa", null); // palaa painike
        AlertDialog alertbuilder = builder.create();

        // Painamalla valittua dataa, sen voi poistaa
        dataListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                //(parent, view, position, id) -> {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String tuotteenTiedotPoistettaessa = (String) dataListView.getItemAtPosition( position );
                String[] tuotteenTiedotPoistettaessaSplitattuna = tuotteenTiedotPoistettaessa.split(" - "); // Splitataan " - " merkeillä
                String tuotteenPainoGrammoinaPoistettaessa = tuotteenTiedotPoistettaessaSplitattuna[2].substring(0, tuotteenTiedotPoistettaessaSplitattuna[2].length() -2); // Päätellään merkkijonosta; huom. lopusta suodatatetaan ".0 g" pois!
                String tuotteenArvoEuroissaPoistettaessa = tuotteenTiedotPoistettaessaSplitattuna[3].substring(0, tuotteenTiedotPoistettaessaSplitattuna[3].length() -2); // Päätellään merkkijonosta;

                AlertDialog.Builder builder2 = new AlertDialog.Builder(eranSkannausActivity.this);
                builder2.setTitle("Tuotteen poistaminen");
                builder2.setMessage("Haluatko varmasti poistaa valitun tuotteen?\n\n" + tuotteenTiedotPoistettaessa);
                builder2.setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        tuoteryhmanSkannaukset.remove(position); // Poistetaan
                        dataAdapter.notifyDataSetChanged(); // päivitetään
                        //System.out.println("Tuotteita: " + tuoteryhmanSkannaukset.size() + " kpl");
                        // Jos määrä meni nollaan
                        if(tuoteryhmanSkannaukset.size() == 0) {
                            //.out.println("Meni nollaan");
                            alertbuilder.setTitle("Tuoteryhmässä ei enempää skannattuja tuotteita!"); // Muutetaan messagea
                            // Poistetaan tuoteryhmä kokonaan listviewistä
                            poistaTuoteryhmaListviewista(tuotekoodi);
                        }
                        // Päivitetään tuoteryhmien listaus
                        paivitaTuoteryhmanYhteenveto(tuotekoodi);
                        arr.notifyDataSetChanged();

                        // Muokataan myös kokonaisyhteenvetoa
                        skannattujenmaara = skannattujenmaara - 1; // Vähennetään skannattujen määrää yhdellä
                        kokopaino = kokopaino - Integer.parseInt(tuotteenPainoGrammoinaPoistettaessa); // Piennetään painoa
                        yhteishinta = yhteishinta - Double.parseDouble(tuotteenArvoEuroissaPoistettaessa.replace(",","."));
                        String paivitettyYhteenveto = "Skannattuja tuotteita " + skannattujenmaara + " kpl, yht. " + Double.toString(((double) kokopaino)/1000) + " kg, " + dfEuro.format(Math.abs(yhteishinta)) + " €";
                        textvievYhteenvetoSkannauksista.setText(paivitettyYhteenveto);
                        // Jos skannattujen määrä meni nollaan, disabloidaan "Lähetä sähköpostiin" -painike
                        if(skannattujenmaara == 0){
                            buttonLahetaSahkopostiin.setEnabled(false);
                        }

                        // Tehdään muutokset myös csv-tiedostoon
                        tallennaMuutoksetCsvTiedostoihin(tiedostonimi);

                    }
                });

                builder2.setNegativeButton("Ei", null);
                builder2.setIcon(R.drawable.ic_launcher_foreground);
                builder2.show();
                return true;
            }
        });


        // Ikkuna näkyviin
        alertbuilder.show();
    }


    /*
    Metodi, joka hoitaa sähköpostin lähetyksen.
     */
    public void lahetaSahkopostiin(){
        // Luodaan uusi alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
        builder.setTitle("Varmista CSV:n lähetys");
        builder.setMessage("Lähetetäänkö erän tiedot CSV-muodossa osoitteeseen " + sharedPref.getString("defaultEmail", null) + "?");
        // Jos jatkettava erä
        if(jatkettavaEra){
            tiedostonimi = jatkettavanEranTiedostonimi;
            eranNimi = tiedostonimi.substring(0, tiedostonimi.length() - 4);
        }
        // Lisätään painikkeet
        builder.setPositiveButton("Lähetä", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lähetetään sähköpostiin halutut tiedostot
                String[] liitetiedostot = {tiedostonimi};
                // Muodostetaan viestin aihe
                String viestinAihe;
                if (eranNimi.length() != 0) {
                    viestinAihe = "Joku hieno nimi: Erän " + eranNimi + " tiedot";
                } else {
                    viestinAihe = "Joku hieno nimi: Nimettömän erän tiedot";
                }
                String viestinSisalto = "Tämä on sovelluksen lähettämä viesti.\n\nHalutun erän skannaustiedot löytyvät liitteinä olevista '" + tiedostonimi + "' ja 'yhteenveto_" + tiedostonimi +"' tiedostoista.";

                // Luodaan Sahkopostiviesti-olio (parametreina context, viestin aihe ja sisältö)
                Sahkopostiviesti sahkopostiviesti = new Sahkopostiviesti(getApplicationContext(), viestinAihe, viestinSisalto, liitetiedostot);
                // Lähetetään sähköpostiviesti (parametrina view ja liitetiedosto)
                sahkopostiviesti.lahetaSahkopostiviesti(findViewById(android.R.id.content));
            }
        });
        builder.setNegativeButton("Peruuta", null);
        // Dialogi näkyviin
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*
    Metodi, joka määrittää mitä tapahtuu, kun käyttäjä klikkaa btAloitaSkannaus-painiketta.
    Oli aiemmin muutakin hienouksia, mutta karsiutuivat pois. Voisi toki yhdistää onCreateen, mutta olkoon näin.
     */
    public void aloitaSkannaus(){
        // Aktivoidaan skanneri
        scanCode();
    }

    /*
    Metodi, joka saa syötteenä tuotekoodin ja palauttaa sen tuotteen nimen ja kilohinnan
     */
    public String[] haeTuotteenTiedot(String luettuTuotekoodi){
        return tuotteidenTiedot.get(luettuTuotekoodi); // Hakee tiedot HashMapista
    }

    /*
    Skannauksen toiminnallisuus
     */
    private void scanCode() {
        // Asetetaan asetukset viivakoodinlukijalle.
        ScanOptions options = new ScanOptions();
        options.setPrompt("---");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);

        try {
            // Luodaan uusi launcher viivakoodinlukijalle ja käynnistetään se annetuilla asetuksilla.
            barLauncher.launch(options);
        } catch (Exception e) {
            // Luodaan uusi hälytysikkuna näyttämään viivakoodinlukijan tulos
            AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
            builder.setTitle("Virhe!");
            builder.setMessage("Kokeile uudestaan!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Sulje kysely ja jatka normaalisti
                    dialog.dismiss();
                    // Aktivoidaan skanneri uudestaan
                    scanCode();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        if (result.getContents() != null) {
            // Tarkistetaan, että viivakoodi on oikeanlainen (esim. alkaa merkeillä "23")
            if (tarkistaViivakoodi(result.getContents())) {
                try {
                    // Pilkotaan skannattu merkkijono
                    splitString(result.getContents());
                    String[] tuotteenTiedot = haeTuotteenTiedot(nextSixChars); // Haetaan tuotteen tiedot funktiolla HashMapista
                    if (tuotteenTiedot != null) {
                        tuotteenNimi = tuotteenTiedot[0];
                        tuotteenKilohinta = tuotteenTiedot[1];
                        tuotteenPainoGrammoina = Integer.parseInt(nextFourChars);
                        Double tuotteenPainoKilogrammoina = new Double(tuotteenPainoGrammoina) / 1000;
                        tuotteenArvoEuroissa = Double.parseDouble(tuotteenKilohinta) * (tuotteenPainoKilogrammoina);

                        // Luodaan uusi hälytysikkuna näyttämään viivakoodinlukijan tulos
                        AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                        // Tulostetaan tiedot ikkunaan
                        builder.setTitle("Skannattu tuote:");
                        builder.setMessage("Tuote: " + nextSixChars + " = " + tuotteenNimi + " (" + tuotteenKilohinta + " €/kg)" +
                                "\nPaino: " + dfGramma.format(tuotteenPainoGrammoina) + " g" +
                                "\nArvo: " + dfEuro.format(tuotteenArvoEuroissa) + " €");

                        // Listaa tuote -painike, jolla skannattu tuote listataan. Ohjelma palaa skanneriin.
                        builder.setPositiveButton("Listaa tuote", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                try {
                                    if(jatkettavaEra == false) {
                                        // Jos kyseessä oli erän ensimmäinen tuote, määritetään tiedostonimi annetun nimen ja aikaleiman perusteella
                                        if (skannattujenmaara == 0 && poistettujenMaara == 0) {
                                            if (eranNimi.length() != 0) {
                                                tiedostonimi = eranNimi + "_" + aikaleima() + ".csv";
                                            } else {
                                                tiedostonimi = "nimeton_" + aikaleima() + ".csv";
                                            }
                                            //buttonLahetaSahkopostiin.setEnabled(true); // Mahdollistetaan sähköpostin lähetys
                                            //buttonEraValmis.setEnabled(true); // Mahdollistetaan erän valmistumisen merkkaaminen
                                        }
                                    }
                                    buttonLahetaSahkopostiin.setEnabled(true); // Mahdollistetaan sähköpostin lähetys
                                    buttonEraValmis.setEnabled(true); // Mahdollistetaan erän valmistumisen merkkaaminen
                                    // Tallennetaan juuri skannattu tuote merkkijonona
                                    String juuriSkannattuTuote = nextSixChars + " - " + tuotteenNimi + " - " + dfGramma.format(tuotteenPainoGrammoina) + " g" + " - " + dfEuro.format(tuotteenArvoEuroissa) + " €";

                                    // Testataan onko tuotetta jo skannattuna HashMapissa; Jos on:
                                    if(skannatutTuotteet.containsKey(nextSixChars)){
                                        //System.out.println("JOOJOO");
                                        // Haetaan ko. tuotteen ArrayList
                                        ArrayList<String> tuotteenSkannaukset =  skannatutTuotteet.get(nextSixChars);
                                        // Alla oleva if tarpeellinen, jotta listaa tuoteryhmän sen jälkeen jos on kertaalleen kaikki tuotteet poistettu
                                        if(tuotteenSkannaukset.size() == 0){
                                            // Lisätään tiedot listviewiin
                                            listatutTuotteet.add(nextSixChars + " - " + tuotteenNimi + " (1 kpl) - " + dfGramma.format(tuotteenPainoGrammoina) + " g" + " - " + dfEuro.format(tuotteenArvoEuroissa) + " €");
                                            // Päivitetään listview
                                            arr.notifyDataSetChanged();
                                        }
                                        // Lisätään juuri skannattu tuote ArrayListiin
                                        tuotteenSkannaukset.add(juuriSkannattuTuote);
                                        // Päivitetään arrayList hashmappiin putilla
                                        skannatutTuotteet.put(nextSixChars, tuotteenSkannaukset);
                                        // Päivitetään listviewissä näkyvät ko. tuoteryhmän tiedot
                                        paivitaTuoteryhmanYhteenveto(nextSixChars);

                                    // Jos ei ole:
                                    } else {
                                        //System.out.println("JEEJEE");
                                        // Alustetaan merkkijono ArrayList
                                        ArrayList<String> tuotteenSkannaukset2 = new ArrayList<String>();
                                        // Lisätään arraylistiin juuri skannattu tuote merkkijonona
                                        tuotteenSkannaukset2.add(juuriSkannattuTuote);
                                        // Luodaan HashMappiin uusi avain-arvo -pari
                                        skannatutTuotteet.put(nextSixChars, tuotteenSkannaukset2);
                                        // Lisätään tiedot listviewiin
                                        listatutTuotteet.add(nextSixChars + " - " + tuotteenNimi + " (1 kpl) - " + dfGramma.format(tuotteenPainoGrammoina) + " g" + " - " + dfEuro.format(tuotteenArvoEuroissa) + " €");
                                        // Päivitetään listview
                                        arr.notifyDataSetChanged();
                                    }

                                    // Muokataan yhteenvetoa
                                    skannattujenmaara = skannattujenmaara + 1;
                                    kokopaino = kokopaino + tuotteenPainoGrammoina;
                                    yhteishinta = yhteishinta + Double.parseDouble(dfEuro.format(tuotteenArvoEuroissa).replace(",",".")); // oltava näin, jotta käyttää sentin tarkkuudella pyöristettyjä arvoja
                                    String paivitettyYhteenveto = "Skannattuja tuotteita " + skannattujenmaara + " kpl, yht. " + Double.toString(((double) kokopaino) / 1000) + " kg, " + dfEuro.format(yhteishinta) + " €";
                                    textvievYhteenvetoSkannauksista.setText(paivitettyYhteenveto);
                                    // Tallennetaan muutokset csv-tiedostoon; luo/kirjoittaa yli tiedoston tiedostonimen perusteella aina kun uusi tuote on skannattu ja listattu
                                    tallennaMuutoksetCsvTiedostoihin(tiedostonimi);

                                    // Sulje kysely ja jatka normaalisti
                                    dialog.dismiss();
                                    // Aktivoidaan skanneri uudestaan
                                    scanCode();
                                    //barLauncher.launch(options); // <- Ilmeisesti turha kun toimii pois kommentoitunakin?


                                } catch (Exception e) {
                                    e.printStackTrace();
                                    // Luodaan uusi hälytysikkuna näyttämään viivakoodinlukijan tulos
                                    AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                                    builder.setTitle("Virhe!");
                                    builder.setMessage("Skannatessa tapahtui odottamaton virhe (1). Kokeile uudestaan!");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Sulje kysely ja jatka normaalisti
                                            dialog.dismiss();
                                            // Aktivoidaan skanneri uudestaan
                                            scanCode();
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }
                        });

                        // Hylkää painike, jotta käyttäjä voi tarvittaessa peruuttaa vahinkoskannauksen
                        builder.setNeutralButton("Hylkää skannaus", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Sulje kysely ja jatka normaalisti
                                dialog.dismiss();
                                // Aktivoidaan skanneri uudestaan
                                scanCode();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    // Jos tuotetta ei löytynyt:
                    } else {
                        // Luodaan uusi hälytysikkuna
                        AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                        builder.setTitle("Skannattua tuotetta ei löytynyt");
                        builder.setMessage("Viivakoodilla " + result.getContents() + " (tuotekoodi " + nextSixChars + ") ei löytynyt yhtään tuotetta.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Sulje kysely ja jatka normaalisti
                                dialog.dismiss();
                                // Aktivoidaan skanneri uudestaan
                                scanCode();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    // Jos skannatessa tulee joku virhe:
                } catch (RuntimeException e) {
                    // Luodaan uusi hälytysikkuna
                    AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                    builder.setTitle("Virhe!");
                    builder.setMessage("Skannauksessa tapahtui odottamaton virhe. Kokeile uudestaan!");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Sulje kysely ja jatka normaalisti
                            dialog.dismiss();
                            // Aktivoidaan skanneri uudestaan
                            scanCode();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            // Jos viivakoodi ei ollut kelvollinen (esim. alkoi muilla merkeillä kuin "23")
            } else {
                // Luodaan uusi hälytysikkuna
                AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                builder.setTitle("Viivakoodia ei tunnistettu");
                builder.setMessage("Viivakoodi oli virheellinen tai se ei ala vaaditulla merkkijonolla (23).");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Sulje kysely ja jatka normaalisti
                        dialog.dismiss();
                        // Aktivoidaan skanneri uudestaan
                        scanCode();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    });

    /*
    Metodi, joka päivittää listViewissä näkyvän tuoteryhmän yhteenvedon. Palauttaa merkkijonon.
     */
    public void paivitaTuoteryhmanYhteenveto(String tuotekoodi){
        // Haetaan ko. tuotteen tuotenimi
        String tuotenimi = tuotteidenTiedot.get(tuotekoodi)[0];
        // Haetaan ko. tuotteen arraylist
        ArrayList<String> tuotteenSkannaukset = skannatutTuotteet.get(tuotekoodi);
        int tuotteidenMaara = tuotteenSkannaukset.size();
        // Jos tuotteita on olemassa (periaatteessa ne voidaan poistaa kaikki)

        // Alustetaan apumuuttujat
        int tuotteidenYhteispaino = 0;
        double tuotteidenYhteisarvo = 0.00;
        // Loopataan arraylistin sisältö läpi ja kasvatetaan painoa ja arvoa
        for (int i = 0; i < tuotteenSkannaukset.size(); i++) {
            String tuotteenTiedot = tuotteenSkannaukset.get(i);
            String[] tuotteenTiedotTaulukossa = tuotteenTiedot.split(" - ");
            tuotteidenYhteispaino = tuotteidenYhteispaino + Integer.parseInt(tuotteenTiedotTaulukossa[2].substring(0, tuotteenTiedotTaulukossa[2].length() - 2));
            tuotteidenYhteisarvo = tuotteidenYhteisarvo + Double.parseDouble(tuotteenTiedotTaulukossa[3].substring(0, tuotteenTiedotTaulukossa[3].length() - 2).replace(",", "."));
        }
        String uusiTuotteenYhteenveto = tuotekoodi + " - " + tuotenimi + " (" + tuotteidenMaara + " kpl) - " + tuotteidenYhteispaino + " g - " + dfEuro.format(tuotteidenYhteisarvo) + " €";
        // Loopataan listatutTuotteet lapi ja paivitetaan oikeasta kohdasta
        for (int j = 0; j < listatutTuotteet.size(); j++) {
            String tuotteenTuotekoodiYhteenvedosta = listatutTuotteet.get(j).substring(0, 6);
            if (tuotteenTuotekoodiYhteenvedosta.equals(tuotekoodi)) {
                listatutTuotteet.set(j, uusiTuotteenYhteenveto);
                arr.notifyDataSetChanged();
                break;
            }
        }


    }

    /*
    Metodi, joka tallentaa muutokset csv-tiedostoon. Käytännössä ylikirjoittaa edellisen samannimisen tiedoston.
     */
    public void tallennaMuutoksetCsvTiedostoihin(String tiedostonimi){
        String tallennettavanTiedostonSisalto = "Tuotekoodi" + erotinmerkki + "Tuotenimi" + erotinmerkki + "PainoGrammoina" + erotinmerkki + "KilohintaSkannaushetkella" + erotinmerkki + "ArvoEuroissa";
        String tallennettavanYhteenvetotiedostonSisalto = "Tuotekoodi" + erotinmerkki + "Tuotenimi" + erotinmerkki + "Kappalemaara" + erotinmerkki + "YhteispainoGrammoina" + erotinmerkki + "KilohintaSkannaushetkella" + erotinmerkki + "YhteisarvoEuroissa";
        String tallennettavanTuoteryhmanTiedot;
        String tallennettavanTuoteryhmanTiedotCsvMuodossa;
        String tallennettavanTuotteenTiedotCsvMuodossa;
        // Jos ollaan jatkamassa aiempaa erää, korvataan tiedostonimi uudella
        if(jatkettavaEra){
            tiedostonimi = jatkettavanEranTiedostonimi;
        }
        String tiedostonimi2 = "yhteenveto_" + tiedostonimi;
        // Käydään listviewissä olevat tuoteryhmät läpi for-silmukassa
        for (int i = 0; i < listviewListatutTuotteet.getCount(); i++){
            tallennettavanTuoteryhmanTiedot = listviewListatutTuotteet.getItemAtPosition(i).toString();
            String[] tallennettavanTuoteryhmanTiedotSplitattuna = tallennettavanTuoteryhmanTiedot.split(" - "); // Splitataan " - " merkeillä
            String tuotteenTuotekoodi = tallennettavanTuoteryhmanTiedotSplitattuna[0]; // Päätellään merkkijonosta
            String[] haetutTuotteenTiedot = haeTuotteenTiedot(tuotteenTuotekoodi); // Haetaan nimi ja kilohinta tiedostosta
            String tuotteenTuotenimi = haetutTuotteenTiedot[0]; // Voisi toki olla myös suoraan: tuotteenTiedotSplitattuna[1]
            Double tuotteenKilohintaSkannaushetkella = Double.parseDouble(haetutTuotteenTiedot[1]); // Otetaan haettu tieto
            // Päätellään myös tuoteryhmän kappalemäärä
            String tuotenimiJaLkm = tallennettavanTuoteryhmanTiedotSplitattuna[1].substring(0, tallennettavanTuoteryhmanTiedotSplitattuna[1].length() -5); // Lopusta " kpl)" pois
            String tuoteryhmanKappalemaara = tuotenimiJaLkm.substring(tuotteenTuotenimi.length()+2, tuotenimiJaLkm.length());
            // Päätellään myös tuoteryhmän yhteispaino
            String tuoteryhmanYhteispaino = tallennettavanTuoteryhmanTiedotSplitattuna[2].substring(0, tallennettavanTuoteryhmanTiedotSplitattuna[2].length() -2); // Lopusta " g" pois
            // Päätellään myös tuoteryhmän yhteisarvo
            String tuoteryhmanYhteisarvo = tallennettavanTuoteryhmanTiedotSplitattuna[3].substring(0, tallennettavanTuoteryhmanTiedotSplitattuna[3].length() -2); // Lopusta " €" pois
            // Tallennetaan yhteenvedon tiedot
            tallennettavanTuoteryhmanTiedotCsvMuodossa =    tuotteenTuotekoodi +
                                                            erotinmerkki +
                                                            tuotteenTuotenimi +
                                                            erotinmerkki +
                                                            tuoteryhmanKappalemaara +
                                                            erotinmerkki +
                                                            tuoteryhmanYhteispaino +
                                                            erotinmerkki +
                                                            tuotteenKilohintaSkannaushetkella +
                                                            erotinmerkki +
                                                            tuoteryhmanYhteisarvo.replace(",", ".");;
            tallennettavanYhteenvetotiedostonSisalto = tallennettavanYhteenvetotiedostonSisalto + "\n" + tallennettavanTuoteryhmanTiedotCsvMuodossa;


            // Nyt kun tuotekoodi on selvillä, haetaan tuotteen skannaukset HashMapista
            ArrayList<String> tuoteryhmanSkannaukset = skannatutTuotteet.get(tuotteenTuotekoodi);
            // Käydään ArrayList läpi
            for (int j = 0; j < tuoteryhmanSkannaukset.size(); j++) {
                String tallennettavanTuotteenTiedot = tuoteryhmanSkannaukset.get(j);
                String[] tallennettavanTuotteenTiedotSplitattuna = tallennettavanTuotteenTiedot.split(" - ");
                String tuotteenPainoGrammoina = tallennettavanTuotteenTiedotSplitattuna[2].substring(0, tallennettavanTuotteenTiedotSplitattuna[2].length() -2); // Päätellään merkkijonosta; huom. lopusta suodatatetaan " g" pois!
                Double tuotteenArvo = (Double.parseDouble(tuotteenPainoGrammoina)/1000)*tuotteenKilohintaSkannaushetkella;
                tallennettavanTuotteenTiedotCsvMuodossa =   tuotteenTuotekoodi +
                                                            erotinmerkki +
                                                            tuotteenTuotenimi +
                                                            erotinmerkki +
                                                            tuotteenPainoGrammoina +
                                                            erotinmerkki +
                                                            tuotteenKilohintaSkannaushetkella +
                                                            erotinmerkki +
                                                            (dfEuro.format(tuotteenArvo)).replace(",", ".");
                tallennettavanTiedostonSisalto = tallennettavanTiedostonSisalto + "\n" + tallennettavanTuotteenTiedotCsvMuodossa;
            }

        }
        // Tallennetaan tiedostojen getFilesDir/csv_tiedostot ja getFilesDir/yhteenvedot kansioon
        try {
            String folder = context.getFilesDir().getAbsolutePath() + File.separator + "csv_tiedostot";
            String folder2 = context.getFilesDir().getAbsolutePath() + File.separator + "yhteenvedot";
            File subFolder = new File(folder);
            File subFolder2 = new File(folder2);
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }
            // Tarkistetaan myös yhteenvedot kansion olemassa olo
            if (!subFolder2.exists()) {
                subFolder2.mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(new File(subFolder, tiedostonimi));
            outputStream.write(tallennettavanTiedostonSisalto.getBytes());
            outputStream.close();
            FileOutputStream outputStream2 = new FileOutputStream(new File(subFolder2, tiedostonimi2));
            outputStream2.write(tallennettavanYhteenvetotiedostonSisalto.getBytes());
            outputStream2.close();
        } catch (IOException e) {
            // Luodaan uusi hälytysikkuna
            AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
            builder.setTitle("Virhe!");
            builder.setMessage("CSV-tiedoston tallentaminen epäonnistui. Kokeile uudestaan.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Sulje kysely ja jatka normaalisti
                    dialog.dismiss();
                    // Aktivoidaan skanneri uudestaan
                    scanCode();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // jakaa barcoden eri osat muuttujiin
    public void splitString(String x) {
        //firstTwoChars = x.substring(0, 2);
        nextSixChars = x.substring(2, 8);
        nextFourChars = x.substring(8, 12);
        //lastChar = x.substring(12, 13);
    }

    public static String aikaleima() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm:ss");
        return currentDateTime.format(formatter);
    }

    public static boolean tarkistaViivakoodi(String y) {
        if (y == null || y.length() != 13) {
            return false; // y on null tai ei ole 13 merkkiä pitkä
        }

        // tarkista, että y sisältää vain numeroita
        for (char c : y.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false; // y sisältää muita kuin numeroita
            }
        }

        // tarkista, että kahdella ensimmäisellä merkillä on arvot "2" ja "3"
        if (y.charAt(0) != '2' || y.charAt(1) != '3') {
            return false; // kahdella ensimmäisellä merkillä ei ole arvoja "2" ja "3"
        }

        return true; // y on kelvollinen
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
                // Kutsutaan erillistä metodia joka tuottaa alertdialogin
                poistutaankoSkannauksesta(0).show();
        }

        return super.onOptionsItemSelected(item);
    }
    /*
    Metodi, joka palauttaa ohjelman päävalikkoon, jos käyttäjä painaa androidin BackButtonia (alalaidassa). Kysyy käyttäjältä varmistuksen.
     */
    @Override
    public void onBackPressed() {
        // Kutsutaan erillistä metodia joka tuottaa alertdialogin
        poistutaankoSkannauksesta(0).show();
    }

    /*
    Metodi, joka määrittää mitä tapahtuu, kun käyttäjä klikkaa btEraValmis-painiketta
     */
    public void kuittaaEraValmiiksi(){
        // Kutsutaan erillistä metodia joka tuottaa alertdialogin
        poistutaankoSkannauksesta(1).show();
    }

    /*
    Metodi, joka palauttaa AlertDialogin, jolla voi poistua skannaustilasta takaisin päävalikkoon.
    Parametri int Valinta riippuu mistä painikkeesta funktiota kutsutaan ja muokkaa tekstejä sen mukaan.
     */
    public AlertDialog poistutaankoSkannauksesta(int valinta){
        // Varmistetaan, että käyttäjä todella haluaa keskeyttää skannauksen ja palata alkuvalikkoon
        AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
        if(jatkettavaEra){
            builder.setTitle("Lopetetaanko skannaus?");
            builder.setMessage("Oletko varma, että haluat keskeyttää erän " + jatkettavanEranTiedostonimi + " skannauksen ja palata alkuvalikkoon?");
        } else {
            // Jos valinta on 0, on funktiota kutsuttu backbuttonilla.
            if (valinta == 0) {
                builder.setTitle("Lopetetaanko skannaus?");
                if (eranNimi.length() == 0) {
                    builder.setMessage("Oletko varma, että haluat keskeyttää erän skannauksen ja poistua alkuvalikkoon?");
                } else {
                    builder.setMessage("Oletko varma, että haluat keskeyttää erän '" + eranNimi + "' skannauksen ja poistua alkuvalikkoon?");
                }
                // Jos valinta on 1, on funktiota kutsuttu Erä valmis -painikkeesta.
            } else if (valinta == 1) {
                builder.setTitle("Onko erä valmis?");
                if (eranNimi.length() == 0) {
                    builder.setMessage("Oletko varma, että koko erä on skannattu ja haluat poistua alkuvalikkoon?");
                } else {
                    builder.setMessage("Oletko varma, että koko erä '" + eranNimi + "' on skannattu ja haluat poistua alkuvalikkoon?");
                }
            }
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
        // Palautetaan dialogi
        AlertDialog alert = builder.create();
        return alert;
    }
}

    /*
    Metodi, joka määrittää mitä tapahtuu, kun käyttäjä klikkaa btLuoKuitti-painiketta
    */
    /*
    public void luoKuitti(){
        // Luodaan kansio "Kuitit" sovelluksen juureen, jos sitä ei vielä ole olemassa.
        File dir = new File(context.getFilesDir(), "Kuitit");
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            TextView txtView = (TextView)findViewById(R.id.tvSkannaustila);
            File gpxfile = new File(dir, aikaleima1() + ".csv");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(txtView.getText());
            writer.flush();
            writer.close();
            System.out.println("Tiedosto löytyy täältä: " + gpxfile.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    */

    /*
    // generoi random id:n
    public static String generateRandomID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }*/

/*
    Metodi, joka määrittää mitä tapahtuu, kun käyttäjä klikkaa ibPoistaValitut-painiketta.

    public void poistaValitutTuotteet(){
        int valittujenMaara = 0;
        boolean tuotteitaPoistettiin = false;
        // Käydään listview läpi käänteisessä järjestyksessä ja poistetaan item jos chekattu
        for (int i = listviewListatutTuotteet.getCount()-1; i >= 0; i--){
            if(listviewListatutTuotteet.isItemChecked(i)){
                String tuotteenTiedotPoistettaessa = listviewListatutTuotteet.getItemAtPosition(i).toString();
                String[] tuotteenTiedotPoistettaessaSplitattuna = tuotteenTiedotPoistettaessa.split(" - "); // Splitataan " - " merkeillä
                String tuotteenPainoGrammoinaPoistettaessa = tuotteenTiedotPoistettaessaSplitattuna[2].substring(0, tuotteenTiedotPoistettaessaSplitattuna[2].length() -2); // Päätellään merkkijonosta; huom. lopusta suodatatetaan ".0 g" pois!
                String tuotteenArvoEuroissaPoistettaessa = tuotteenTiedotPoistettaessaSplitattuna[3].substring(0, tuotteenTiedotPoistettaessaSplitattuna[3].length() -2); // Päätellään merkkijonosta;
                valittujenMaara++; // Kasvatetaan valittujen määrää yhdellä
                listatutTuotteet.remove(i); // Poistetaan tuote
                poistettujenMaara++; // Kasvatetaan poistettujen määrää yhdellä
                tuotteitaPoistettiin = true; // Merkitään, että tuotteita poistettiin jotta tiedetään päivittää myös csv-tiedosto
                arr.notifyDataSetChanged(); // Päivitetään myös listview
                // Muokataan yhteenvetoa
                skannattujenmaara = skannattujenmaara - 1; // Vähennetään skannattujen määrää yhdellä
                kokopaino = kokopaino - Integer.parseInt(tuotteenPainoGrammoinaPoistettaessa); // Piennetään painoa
                yhteishinta = yhteishinta - Double.parseDouble(tuotteenArvoEuroissaPoistettaessa.replace(",","."));
                String paivitettyYhteenveto = "Skannattuja tuotteita " + skannattujenmaara + " kpl, yht. " + Double.toString(((double) kokopaino)/1000) + " kg, " + dfEuro.format(Math.abs(yhteishinta)) + " €";
                textvievYhteenvetoSkannauksista.setText(paivitettyYhteenveto);
                // Jos skannattujen määrä meni nollaan, disabloidaan "Lähetä sähköpostiin" -painike
                if(skannattujenmaara == 0){
                    buttonLahetaSahkopostiin.setEnabled(false);
                }
            }
        }
        if(tuotteitaPoistettiin){
            // Tehdään muutokset myös csv-tiedostoon
            tallennaMuutoksetCsvTiedostoihin(tiedostonimi);
        }
        // Poistetaan valinnat checkboxeista
        listviewListatutTuotteet.clearChoices();
        arr.notifyDataSetChanged();
        // Tulostetaan alertdialog, jos yhtään tuotetta ei ollut valittuna
        if(valittujenMaara == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
            builder.setTitle("Tuotteiden poistaminen");
            builder.setMessage("Yhtään tuotetta ei ole valittu!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int i)
                {
                    // Sulje kysely ja jatka normaalisti
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };
    */