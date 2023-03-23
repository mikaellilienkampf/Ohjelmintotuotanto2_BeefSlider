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
