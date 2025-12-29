package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class Pizza extends Produs {
    public enum TipBlat { SUBTIRE, CLASIC, PUFOS }
    public enum TipSos { ROSU, ALB }
    public enum Topping {
        MOZZARELLA(3.0), SOPRANO(4.0), CIUPERCI(2.0), MASLINE(2.0), ARDEI(2.0), PORUMB(2.0), ANANAS(3.0), SALAM(4.0), SUNCA(4.0);
        private final double pret;
        Topping(double pret) { this.pret = pret; }
        public double pret() { return pret; }
    }

    private final TipBlat blat;
    private final TipSos sos;
    private final List<Topping> toppings;

    private Pizza(String nume, double pret, boolean vegetarian, TipBlat blat, TipSos sos, List<Topping> toppings) {
        super(nume, pret, vegetarian);
        this.blat = blat;
        this.sos = sos;
        this.toppings = List.copyOf(toppings);
    }

    public TipBlat getBlat() { return blat; }
    public TipSos getSos() { return sos; }
    public List<Topping> getToppings() { return toppings; }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        toppings.forEach(t -> sj.add(t.name()));
        String topp = toppings.isEmpty() ? "fara topping" : sj.toString();
        String veg = isVegetarian() ? " (V)" : "";
        return getNume() + " (" + blat + ", sos " + sos + ", " + topp + ")" + veg + " - " + String.format("%.1f", getPret()) + " RON";
    }

    public static class Builder {
        private final TipBlat blat;
        private final TipSos sos;
        private final List<Topping> toppings = new ArrayList<>();
        private String nume = "Pizza Personalizata";
        private double baza = 30.0;
        private boolean vegetarian = false;

        public Builder(TipBlat blat, TipSos sos) {
            this.blat = Objects.requireNonNull(blat, "blat");
            this.sos = Objects.requireNonNull(sos, "sos");
        }

        public Builder nume(String nume) { if (nume != null && !nume.isBlank()) this.nume = nume; return this; }
        public Builder baza(double baza) { if (baza >= 0) this.baza = baza; return this; }
        public Builder vegetarian(boolean vegetarian) { this.vegetarian = vegetarian; return this; }
        public Builder adaugaTopping(Topping t) { if (t != null) this.toppings.add(t); return this; }
        public Builder adaugaToppingi(List<Topping> ts) { if (ts != null) ts.forEach(this::adaugaTopping); return this; }

        public Pizza build() {
            double pret = baza + toppings.stream().mapToDouble(Topping::pret).sum();
            return new Pizza(nume, pret, vegetarian, blat, sos, toppings);
        }
    }
}
