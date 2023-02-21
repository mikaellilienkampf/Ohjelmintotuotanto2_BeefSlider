package com.bonuscarnisapp;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Lista
 *
 *
 * @author R13
 * @version 1.00 2023/02/13
 */

public class Lista {
    private int id;
    private String nimi;
    private LocalDateTime aika;
    private static ArrayList<Rivi> rivit = new ArrayList<Rivi>();
    private static ArrayList<Lista> listat = new ArrayList<Lista>();

    Lista() {
    }

    Lista(int id, String nimi) {
        this.id = id;
        this.nimi = nimi;
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

    public LocalDateTime getAika() {
        return aika;
    }

    public void setAika(LocalDateTime aika) {
        this.aika = aika;
    }

    /**
     * toString
     *
     * @return String id,nimike,hinta
     */
    @NonNull
    public String toString() {
        return id + "," + nimi + "," + aika;
    }

}