package com.bonuscarnisapp;

import androidx.annotation.NonNull;

/**
 * Rivi
 *
 *
 * @author R13
 * @version 1.00 2023/02/13
 */

public class Rivi {
    private int tuoteId;
    private int paino;

    Rivi() {
    }

    Rivi(int tuoteId, int paino) {
        this.tuoteId = tuoteId;
        this.paino = paino;
    }

    public int getTuoteId() {
        return tuoteId;
    }

    public void setTuoteId(int tuoteId) {
        this.tuoteId = tuoteId;
    }

    public int getPaino() {
        return paino;
    }

    public void setPaino(int paino) {
        this.paino = paino;
    }

    /**
     * toString
     *
     * @return String id,nimike,hinta
     */
    @NonNull
    public String toString() {
        return tuoteId + "," + paino;
    }

}