package org.example;

public final class Mancare extends Produs {
    private final int gramaj;

    public Mancare(String nume, double pret, int gramaj, boolean vegetarian) {
        super(nume, pret, vegetarian);
        if (gramaj <= 0) throw new IllegalArgumentException("Gramajul trebuie sa fie > 0");
        this.gramaj = gramaj;
    }

    public int getGramaj() {
        return gramaj;
    }

    @Override
    public String toString() {
        String veg = isVegetarian() ? " (V)" : "";
        return super.toString() + " - Gramaj: " + gramaj + "g" + veg;
    }
}
