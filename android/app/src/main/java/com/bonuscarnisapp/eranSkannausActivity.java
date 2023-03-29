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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;

public class eranSkannausActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private boolean isDarkTheme;
    private Button buttonAktivoiSkanneri;
    private Button buttonEraValmis;
    private Button buttonLahetaSahkopostiin;
    private ImageButton imagebuttonPoistaValitut;
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

    String firstTwoChars;
    String nextSixChars;
    String nextFourChars;
    String lastChar;
    int skannattujenmaara;
    int poistettujenMaara = 0;
    private static final DecimalFormat dfEuro = new DecimalFormat("0.00");
    private static final DecimalFormat dfGramma = new DecimalFormat("0");
    int kokopaino;
    Double yhteishinta = 0.00;


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
        eranNimi = getIntent().getExtras().getString("eranNimiAvain");
        if (eranNimi.length() != 0) {
            actionBar.setTitle("Erä: " + eranNimi);
        } else {
            actionBar.setTitle("Nimetön erä");
        }

        // Alustetaan listatut tuotteet
        listatutTuotteet = new ArrayList<String>();
        // Luodaan arrayAdapter listviewille
        listviewListatutTuotteet = findViewById(R.id.lvListatutTuotteet);
        arr = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_checked,
                listatutTuotteet);
        listviewListatutTuotteet.setAdapter(arr);
        listviewListatutTuotteet.setChoiceMode(listviewListatutTuotteet.CHOICE_MODE_MULTIPLE); // Voi valita monta itemiä
        listviewListatutTuotteet.setTranscriptMode(listviewListatutTuotteet.TRANSCRIPT_MODE_ALWAYS_SCROLL); // Scrollaa automaattisesti alas, jos tuotteita tulee paljon
        // Textview, johon yhteenveto skannatuista tuotteista
        textvievYhteenvetoSkannauksista = findViewById(R.id.tvYhteenvetoSkannauksista);

        // buttonAloitaSkannaus -painikkeen toiminnallisuus
        buttonAktivoiSkanneri = findViewById(R.id.btAktivoiSkanneri);
        buttonAktivoiSkanneri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aloitaSkannaus();
            }
        });

        // buttonKuittinappi -painikkeen toiminnallisuus
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

        //buttonPoistaValitut -painikkeen toiminnallisuus
        imagebuttonPoistaValitut = findViewById(R.id.ibPoistaValitut);
        imagebuttonPoistaValitut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Poistetaan listviewistä ne tuotteet, jotka on valittuna
                poistaValitutTuotteet();
            }
        });
        // Alustetaan kontekstimuuttuja
        context = getApplicationContext();

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
    }

    /*
    Metodi, joka hoitaa sähköpostin lähetyksen.
     */
    public void lahetaSahkopostiin(){
        // Luodaan uusi alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
        builder.setTitle("Varmista CSV:n lähetys");
        builder.setMessage("Lähetetäänkö erän tiedot CSV-muodossa osoitteeseen " + sharedPref.getString("defaultEmail", null) + "?");

        // Lisätään painikkeet
        builder.setPositiveButton("Lähetä", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lähetetään sähköpostiin halutut tiedostot
                String[] liitetiedostot = {tiedostonimi};
                // Muodostetaan viestin aihe
                String viestinAihe;
                if (eranNimi.length() != 0) {
                    viestinAihe = "Lihalinko: Erän " + eranNimi + " tiedot";
                } else {
                    viestinAihe = "Lihalinko: Nimettömän erän tiedot";
                }
                String viestinSisalto = "Tämä on sovelluksen lähettämä viesti.\n\nHalutun erän skannaustiedot löytyvät liitteenä olevasta '" + tiedostonimi + "' tiedostosta.";

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
            // Tarkistetaan, että viivakoodi on oikeanlainen (esim. alkaa merkeillä "28")
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
                                    // Jos kyseessä oli erän ensimmäinen tuote, määritetään tiedostonimi annetun nimen ja aikaleiman perusteella
                                    if (skannattujenmaara == 0 && poistettujenMaara == 0) {
                                        if (eranNimi.length() != 0) {
                                            tiedostonimi = eranNimi + "_" + aikaleima() + ".csv";
                                        } else {
                                            tiedostonimi = "nimeton_" + aikaleima() + ".csv";
                                        }
                                        buttonLahetaSahkopostiin.setEnabled(true); // Mahdollistetaan sähköpostin lähetys
                                        buttonEraValmis.setEnabled(true); // Mahdollistetaan erän valmistumisen merkkaaminen
                                    }

                                    // Lisätään luettu tuote listviewiin
                                    listatutTuotteet.add(nextSixChars + " - " + tuotteenNimi + " - " + dfGramma.format(tuotteenPainoGrammoina) + " g" + " - " + dfEuro.format(tuotteenArvoEuroissa) + " €");

                                    // Päivitetään listview
                                    arr.notifyDataSetChanged();
                                    // Muokataan yhteenvetoa
                                    skannattujenmaara = skannattujenmaara + 1;
                                    kokopaino = kokopaino + tuotteenPainoGrammoina;
                                    yhteishinta = yhteishinta + tuotteenArvoEuroissa;
                                    String paivitettyYhteenveto = "Skannattuja tuotteita " + skannattujenmaara + " kpl, yht. " + Double.toString(((double) kokopaino) / 1000) + " kg, " + dfEuro.format(yhteishinta) + " €";
                                    textvievYhteenvetoSkannauksista.setText(paivitettyYhteenveto);
                                    // Tallennetaan muutokset csv-tiedostoon; luo/kirjoittaa yli tiedoston tiedostonimen perusteella aina kun uusi tuote on skannattu ja listattu
                                    tallennaMuutoksetCsvTiedostoon(tiedostonimi);
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
                        builder.setMessage("Viivakoodilla " + result.getContents() + "(tuotekoodi " + nextSixChars + ") ei löytynyt yhtään tuotetta.");
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
            // Jos viivakoodi ei ollut kelvollinen (esim. alkoi muilla merkeillä kuin "28")
            } else {
                // Luodaan uusi hälytysikkuna
                AlertDialog.Builder builder = new AlertDialog.Builder(eranSkannausActivity.this);
                builder.setTitle("Viivakoodia ei tunnistettu");
                builder.setMessage("Viivakoodi oli virheellinen tai se ei ala vaaditulla merkkijonolla (28).");
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
    Metodi, joka tallentaa muutokset csv-tiedostoon. Käytännössä ylikirjoittaa edellisen samannimisen tiedoston.
    // HUOM. Tähän joku oliopohjainen pyörittely?
     */
    public void tallennaMuutoksetCsvTiedostoon(String tiedostonimi){
        String tallennettavanTiedostonSisalto = "Tuotekoodi,Tuotenimi,PainoGrammoina,KilohintaSkannaushetkella,ArvoEuroissa";
        String tallennettavanTuotteenTiedot;
        String tallennettavanTuotteenTiedotCsvMuodossa;
        // Käydään listviewissä olevat tuotteet läpi for-silmukassa ja lisätään CSV-muodossa merkkijonoon
        for (int i = 0; i < listviewListatutTuotteet.getCount(); i++){
            tallennettavanTuotteenTiedot = listviewListatutTuotteet.getItemAtPosition(i).toString();
            String[] tallennettavanTuotteenTiedotSplitattuna = tallennettavanTuotteenTiedot.split(" - "); // Splitataan " - " merkeillä
            String tuotteenTuotekoodi = tallennettavanTuotteenTiedotSplitattuna[0]; // Päätellään merkkijonosta
            String[] haetutTuotteenTiedot = haeTuotteenTiedot(tuotteenTuotekoodi); // Haetaan nimi ja kilohinta tiedostosta
            String tuotteenTuotenimi = haetutTuotteenTiedot[0]; // Voisi toki olla myös suoraan: tuotteenTiedotSplitattuna[1]
            String tuotteenPainoGrammoina = tallennettavanTuotteenTiedotSplitattuna[2].substring(0, tallennettavanTuotteenTiedotSplitattuna[2].length() -2); // Päätellään merkkijonosta; huom. lopusta suodatatetaan " g" pois!
            Double tuotteenKilohintaSkannaushetkella = Double.parseDouble(haetutTuotteenTiedot[1]); // Otetaan haettu tieto
            Double tuotteenArvo = (Double.parseDouble(tuotteenPainoGrammoina)/1000)*tuotteenKilohintaSkannaushetkella;
            tallennettavanTuotteenTiedotCsvMuodossa =   tuotteenTuotekoodi + "," +
                                                        tuotteenTuotenimi + "," +
                                                        tuotteenPainoGrammoina + "," +
                                                        tuotteenKilohintaSkannaushetkella + "," +
                                                        (dfEuro.format(tuotteenArvo)).replace(",", ".");
            tallennettavanTiedostonSisalto = tallennettavanTiedostonSisalto + "\n" + tallennettavanTuotteenTiedotCsvMuodossa;
        }
        // Tallennetaan tiedosto getFilesDir/csv_tiedostot kansioon
        try {
            String folder = context.getFilesDir().getAbsolutePath() + File.separator + "csv_tiedostot";
            File subFolder = new File(folder);
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(new File(subFolder, tiedostonimi));
            outputStream.write(tallennettavanTiedostonSisalto.getBytes());
            outputStream.close();
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

        // tarkista, että kahdella ensimmäisellä merkillä on arvot "2" ja "8"
        if (y.charAt(0) != '2' || y.charAt(1) != '8') {
            return false; // kahdella ensimmäisellä merkillä ei ole arvoja "2" ja "8"
        }

        return true; // y on kelvollinen
    }



    /*
    Metodi, joka määrittää mitä tapahtuu, kun käyttäjä klikkaa ibPoistaValitut-painiketta.
     */
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
            tallennaMuutoksetCsvTiedostoon(tiedostonimi);
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
        // Jos valinta on 0, on funktiota kutsuttu backbuttonilla.
        if(valinta == 0) {
            builder.setTitle("Lopetetaanko skannaus?");
            if (eranNimi.length() == 0) {
                builder.setMessage("Oletko varma, että haluat keskeyttää erän skannauksen ja poistua alkuvalikkoon?");
            } else {
                builder.setMessage("Oletko varma, että haluat keskeyttää erän '" + eranNimi + "' skannauksen ja poistua alkuvalikkoon?");
            }
        // Jos valinta on 1, on funktiota kutsuttu Erä valmis -painikkeesta.
        } else if (valinta == 1){
            builder.setTitle("Onko erä valmis?");
            if (eranNimi.length() == 0) {
                builder.setMessage("Oletko varma, että koko erä on skannattu ja haluat poistua alkuvalikkoon?");
            } else {
                builder.setMessage("Oletko varma, että koko erä '" + eranNimi + "' on skannattu ja haluat poistua alkuvalikkoon?");
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

