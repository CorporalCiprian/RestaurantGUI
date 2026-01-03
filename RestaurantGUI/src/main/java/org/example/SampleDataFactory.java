package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Central place for the sample menu items used across CLI/GUI/DB seeding.
 */
public final class SampleDataFactory {
    private SampleDataFactory() {
    }

    public static List<Produs> createSampleProducts() {
        List<Produs> sampleList = new ArrayList<>();

        Pizza pizza = new Pizza.Builder(Pizza.TipBlat.CLASIC, Pizza.TipSos.ROSU)
                .nume("Pizza Margherita")
                .vegetarian(true)
                .baza(45.0)
                .build();
        Mancare paste = new Mancare("Paste Carbonara", 52.5, 400, false);
        Mancare supaVeg = new Mancare("Supa Veg", 24.0, 300, true);
        Bautura limonada = new Bautura("Limonada", 15.0, 400, true);
        Bautura apa = new Bautura("Apa Plata", 8.0, 500, true);
        Bautura lichiorOua = new Bautura("Lichior de Oua", 22.0, 50, false);

        Mancare bruschette = new Mancare("Bruschette cu rosii", 18.0, 120, true);
        Mancare salataCaprese = new Mancare("Salata Caprese", 22.0, 150, true);
        Mancare chiftelute = new Mancare("Chiftelute de pui", 20.0, 100, false);

        Mancare tiramisu = new Mancare("Tiramisu", 25.0, 120, true);
        Mancare papanasi = new Mancare("Papanasi cu smantana si gem", 28.0, 200, true);
        Mancare inghetata = new Mancare("Inghetata asortata", 15.0, 100, true);

        sampleList.add(pizza);
        sampleList.add(paste);
        sampleList.add(supaVeg);
        sampleList.add(limonada);
        sampleList.add(apa);
        sampleList.add(lichiorOua);
        sampleList.add(bruschette);
        sampleList.add(salataCaprese);
        sampleList.add(chiftelute);
        sampleList.add(tiramisu);
        sampleList.add(papanasi);
        sampleList.add(inghetata);

        return sampleList;
    }
}

