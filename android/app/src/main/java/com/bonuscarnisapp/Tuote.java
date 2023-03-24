package com.bonuscarnisapp;

import android.text.Editable;

import androidx.annotation.NonNull;

/**
 * Tuote
 *
 *
 * @author R13
 * @version 1.00 2023/02/13
 */

public class Tuote {
    private int id;
    private String nimi;
    private float hinta;


    Tuote(int id, String nimi, Float hinta) {
        this.id = id;
        this.nimi = nimi;
        this.hinta = hinta;
    }

    public static String alkuData() {
        return "591601;Sisäfile;54.95\n" +
                "591602;Ulkofile;44.95\n" +
                "591603;Entrecote;36.95\n" +
                "591604;Sisäpaisti;29.95\n" +
                "591605;Paahtopaisti;29.95\n" +
                "591606;Kulmapaisti;25.95\n" +
                "591607;Ulkopaisti;24.95\n" +
                "591608;Pisarapaisti;25.95\n" +
                "591609;Pyöröpaisti;25.95\n" +
                "591610;Lapapaisti;19.95\n" +
                "591612;Keittoliha (luuton);15.95\n" +
                "591613;Keittoliha (luullinen);14.95\n" +
                "591624;Flank Steak (kuve);21.95\n" +
                "591615;Ribsit;14.95\n" +
                "591620;Tomahawk Pihvi;35.95\n" +
                "591621;T-luu Pihvi;41.95\n" +
                "591622;Lehtipihvi;26.95\n" +
                "591611;Osso Bucco;19.95\n" +
                "591640;Ulkofilepihvi;47.95\n" +
                "591631;Club Steak;37.95\n" +
                "591632;RIB EYE Steak;37.95\n" +
                "591636;Flat Iron Steak;28.95\n" +
                "591634;Kalbi Ribsit;14.95\n" +
                "591614;Jauheliha;14.95\n" +
                "591639;Petite Tender;26.95\n" +
                "591633;Paistikuutio;25.95\n" +
                "591629;Varrasliha;25.95\n" +
                "591637;Picanha;26.95\n" +
                "591638;Maminha;26.95\n" +
                "591628;Niska;23.95\n" +
                "591618;Brisket (Rinta);21.95\n" +
                "591626;Sisäfile pihvi;56.95\n" +
                "591641;Paisti suikale;25.95\n" +
                "591699;Niskakiekot;21.90\n" +
                "591698;Hartiapalat;28.50\n" +
                "591697;Kyljykset;29.50\n" +
                "591696;Lapaviipale;23.90\n" +
                "591695;Kylkirivi;21.90\n" +
                "591694;Kare;28.90\n" +
                "591692;Viulukiekot;28.90\n" +
                "591688;Ulkofile;41.50\n" +
                "591687;Sisäfile;48.50\n" +
                "591685;Lapa;19.90\n" +
                "591684;Viulu;27.50\n" +
                "591683;Kylki;18.90\n" +
                "591682;Luuton Paisti;37.50\n" +
                "591681;Jauheliha;19.90\n" +
                "591678;Potka;19.90\n" +
                "591677;Maksa;12.90\n";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public float getHinta() {
        return hinta;
    }

    public void setHinta(float hinta) {
        this.hinta = hinta;
    }

    /**
     * toString
     *
     * @return String id,nimike,hinta
     */
    @NonNull
    public String toString() {
        return id + ";" + nimi + ";" + hinta;
    }

}
