package org.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        AppConfig config = incarcaConfig();
        if (config == null) {
            System.err.println("Aplicatia nu poate porni fara fisierul de configurare valid.");
            return;
        }

        List<Produs> meniuLista = new ArrayList<>();
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

        meniuLista.add(pizza);
        meniuLista.add(paste);
        meniuLista.add(supaVeg);
        meniuLista.add(limonada);
        meniuLista.add(apa);
        meniuLista.add(lichiorOua);
        meniuLista.add(bruschette);
        meniuLista.add(salataCaprese);
        meniuLista.add(chiftelute);
        meniuLista.add(tiramisu);
        meniuLista.add(papanasi);
        meniuLista.add(inghetata);

        Meniu meniu = new Meniu();
        meniu.adauga(Categorie.FEL_PRINCIPAL, pizza);
        meniu.adauga(Categorie.FEL_PRINCIPAL, paste);
        meniu.adauga(Categorie.FEL_PRINCIPAL, supaVeg);
        meniu.adauga(Categorie.BAUTURI_RACORITOARE, limonada);
        meniu.adauga(Categorie.BAUTURI_RACORITOARE, apa);
        meniu.adauga(Categorie.BAUTURI_ALCOOLICE, lichiorOua);

        meniu.adauga(Categorie.APERITIVE, bruschette);
        meniu.adauga(Categorie.APERITIVE, salataCaprese);
        meniu.adauga(Categorie.APERITIVE, chiftelute);

        meniu.adauga(Categorie.DESERT, tiramisu);
        meniu.adauga(Categorie.DESERT, papanasi);
        meniu.adauga(Categorie.DESERT, inghetata);

        System.out.println("--- Meniul Restaurantului \"" + config.getRestaurantName() + "\" ---");
        for (Produs p : meniuLista) {
            System.out.println("> " + p);
        }

        System.out.println("\n--- Meniu: Fel principal ---");
        meniu.produseDin(Categorie.FEL_PRINCIPAL).forEach(p -> System.out.println("* " + p));

        System.out.println("\n-- Vegetariene in ordine alfabetica --");
        meniu.vegetarieneInOrdineAlfabetica().forEach(p -> System.out.println("* " + p.getNume()));

        System.out.println("Pret mediu la deserturi: " + meniu.pretMediuLa(Categorie.DESERT).orElse(0.0));
        System.out.println("Exista preparat peste 100 RON? " + meniu.existaPestePret(100));

        Optional<Produs> cautat = meniu.cautaDupaNume("pizza margherita");
        System.out.println("Cautare 'pizza margherita' gasit? " + cautat.isPresent());

        Pizza pizzaCustom = new Pizza.Builder(Pizza.TipBlat.CLASIC, Pizza.TipSos.ROSU)
                .nume("Pizza Custom Vegetariana")
                .vegetarian(true)
                .adaugaTopping(Pizza.Topping.MOZZARELLA)
                .adaugaTopping(Pizza.Topping.CIUPERCI)
                .adaugaTopping(Pizza.Topping.MASLINE)
                .build();
        meniu.adauga(Categorie.FEL_PRINCIPAL, pizzaCustom);
        System.out.println("\nNoua pizza: " + pizzaCustom);

        Comanda comanda = new Comanda(config.getTva());
        comanda.adauga(pizza, 2);
        comanda.adauga(paste, 1);
        comanda.adauga(limonada, 3);
        comanda.adauga(apa, 1);

        System.out.println();
        System.out.println("--- Rezumat Comanda ---");
        comanda.getPozitii().forEach((produs, cant) ->
                System.out.println("- " + cant + " x " + produs.getNume() + " @ " + produs.getPret() + " RON"));

        System.out.println("Subtotal: " + comanda.formatMoney(comanda.subtotal()));
        System.out.println("Total (TVA " + (int) (config.getTva() * 100) + "%): " + comanda.formatMoney(comanda.totalCuTvaFaraDiscount()));

        double procent = 0.20;
        Oferta happyHour = c -> c.getPozitii().entrySet().stream()
                .filter(e -> e.getKey() instanceof Bautura)
                .mapToDouble(e -> e.getKey().getPret() * e.getValue())
                .sum() * procent;

        Oferta pizzaPlusBautura = c -> {
            int numarPizze = c.getPozitii().entrySet().stream()
                    .filter(e -> e.getKey() instanceof Pizza)
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            if (numarPizze <= 0) return 0.0;
            List<Double> preturiBauturi = new ArrayList<>();
            c.getPozitii().forEach((produs, cant) -> {
                if (produs instanceof Bautura) {
                    for (int i = 0; i < cant; i++) preturiBauturi.add(produs.getPret());
                }
            });
            if (preturiBauturi.isEmpty()) return 0.0;
            preturiBauturi.sort(Comparator.naturalOrder());
            int gratuit = Math.min(numarPizze, preturiBauturi.size());
            double disc = 0.0;
            for (int i = 0; i < gratuit; i++) disc += preturiBauturi.get(i);
            return disc;
        };

        double d1 = happyHour.calculeazaDiscount(comanda);
        System.out.println("Discount Happy Hour (bauturi -20%): -" + comanda.formatMoney(d1));

        double d2 = pizzaPlusBautura.calculeazaDiscount(comanda);
        System.out.println("Discount Pizza + Bautura gratis: -" + comanda.formatMoney(d2));

        List<Oferta> oferte = new ArrayList<>();
        oferte.add(happyHour);
        oferte.add(pizzaPlusBautura);

        double discTotal = comanda.discountTotal(oferte);
        System.out.println("Discount total (ambele oferte): -" + comanda.formatMoney(discTotal));
        System.out.println("Total final (ambele oferte) + TVA: " + comanda.formatMoney(comanda.totalCuTvaSiDiscounturi(oferte)));

        exportMeniuInJson(meniuLista);
    }

    private static AppConfig incarcaConfig() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("src/main/resources/config.json"), AppConfig.class);
        } catch (JsonParseException | JsonMappingException e) {
            System.err.println("Eroare: Fisierul de configurare este corupt sau are format invalid.");
        } catch (FileNotFoundException e) {
            System.err.println("Eroare: Fisierul de configurare nu a fost gasit. Va rugam contactati suportul tehnic.");
        } catch (IOException e) {
            System.err.println("Eroare la citirea fisierului de configurare: " + e.getMessage());
        }
        return null;
    }

    private static void exportMeniuInJson(List<Produs> meniuLista) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("meniu-export.json"), meniuLista);
            System.out.println("Meniul a fost exportat cu succes in fisierul meniu-export.json");
        } catch (IOException e) {
            System.err.println("Eroare la exportul meniului in JSON: " + e.getMessage());
        }
    }
}