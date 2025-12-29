package org.example;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Comanda {
    private final double tva;
    private final Map<Produs, Integer> pozitii = new LinkedHashMap<>();

    public Comanda(double tva) {
        this.tva = tva;
    }

    public void adauga(Produs produs, int cantitate) {
        if (cantitate <= 0) throw new IllegalArgumentException("Cantitatea trebuie sa fie > 0");
        pozitii.merge(produs, cantitate, Integer::sum);
    }

    public Map<Produs, Integer> getPozitii() {
        return Map.copyOf(pozitii);
    }

    public double subtotal() {
        return pozitii.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPret() * e.getValue())
                .sum();
    }

    public double totalCuTvaFaraDiscount() {
        double sub = subtotal();
        return sub * (1.0 + tva);
    }

    public double totalCuTvaSiDiscount(Oferta oferta) {
        double sub = subtotal();
        double discount = oferta != null ? oferta.calculeazaDiscount(this) : 0.0;
        double dupaDiscount = Math.max(0.0, sub - discount);
        return dupaDiscount * (1.0 + tva);
    }

    public double discountTotal(List<Oferta> oferte) {
        double sub = subtotal();
        double disc = 0.0;
        if (oferte != null) {
            for (Oferta o : oferte) {
                if (o != null) disc += o.calculeazaDiscount(this);
            }
        }
        if (disc < 0) disc = 0.0;
        return Math.min(sub, disc);
    }

    public double totalCuTvaSiDiscounturi(List<Oferta> oferte) {
        double sub = subtotal();
        double disc = discountTotal(oferte);
        double dupaDiscount = Math.max(0.0, sub - disc);
        return dupaDiscount * (1.0 + tva);
    }

    public String formatMoney(double value) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(value) + " RON";
    }
}
