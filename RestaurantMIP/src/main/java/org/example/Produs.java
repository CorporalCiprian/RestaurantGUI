package org.example;

import java.text.DecimalFormat;
import java.util.Objects;

public sealed abstract class Produs permits Mancare, Bautura, Pizza {
    private final String nume;
    private final double pret;
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
        return Double.compare(produs.pret, pret) == 0 && nume.equals(produs.nume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), nume, pret);
    }
}
