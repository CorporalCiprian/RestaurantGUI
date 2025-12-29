package org.example;

public final class Bautura extends Produs {
    private final int volum;

    public Bautura(String nume, double pret, int volum, boolean vegetarian) {
        super(nume, pret, vegetarian);
        if (volum <= 0) throw new IllegalArgumentException("Volumul trebuie sa fie > 0");
        this.volum = volum;
    }

    public int getVolum() {
        return volum;
    }

    @Override
    public String toString() {
        String veg = isVegetarian() ? " (V)" : "";
        return super.toString() + " - Volum: " + volum + "ml" + veg;
    }
}
