package com.aokp.romcontrol.performance;

public class Voltage {
    private String freq;
    private String currentMv;
    private String savedMv;

    public void setFreq(final String freq) {
        this.freq = freq;
    }

    public String getFreq() {
        return freq;
    }

    public void setCurrentMV(final String currentMv) {
        this.currentMv = currentMv;
    }

    public String getCurrentMv() {
        return currentMv;
    }

    public void setSavedMV(final String savedMv) {
        this.savedMv = savedMv;
    }

    public String getSavedMV() {
        return savedMv;
    }
}
