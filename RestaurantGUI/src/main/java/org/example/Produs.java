package org.example;

import java.text.DecimalFormat;
import java.util.Objects;

public sealed abstract class Produs permits Mancare, Bautura, Pizza {
    private final String nume;
    // make pret mutable so GUI can update it in-memory
    private double pret;
    private final boolean vegetarian;

    protected Produs(String nume, double pret, boolean vegetarian) {
        this.nume = Objects.requireNonNull(nume, "nume");
        if (pret < 0) throw new IllegalArgumentException("Pretul trebuie sa fie >= 0");
        this.pret = pret;
        this.vegetarian = vegetarian;
    }

    public String getNume() {
        return nume;
    }

    public double getPret() {
        return pret;
    }

    /**
     * Seteaza pretul (in-memory). Valideaza valoarea (> = 0).
     * Observatie: equals/hashCode nu includ pretul pentru a evita probleme
     * cand obiectele sunt folosite ca chei in harti si pretul se modifica.
     */
    public void setPret(double pret) {
        if (pret < 0) throw new IllegalArgumentException("Pretul trebuie sa fie >= 0");
        this.pret = pret;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    protected String formatPret() {
        DecimalFormat df = new DecimalFormat("#0.0");
        return df.format(pret);
    }

    @Override
    public String toString() {
        return getNume() + " - " + formatPret() + " RON";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produs produs = (Produs) o;
        // Do NOT include pret in equality so that mutable price updates do not
        // break collections/maps that use Produs as key. Equality is defined
        // by runtime class and name only.
        return nume.equals(produs.nume);
    }

    @Override
    public int hashCode() {
        // See note in equals: omit pret from hashCode to keep identity stable
        return Objects.hash(getClass(), nume);
    }
}
