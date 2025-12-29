package org.example;

import java.util.*;
import java.util.function.Predicate;

public class Meniu {
    private final Map<Categorie, List<Produs>> produsePeCategorii = new EnumMap<>(Categorie.class);

    public Meniu() {
        for (Categorie c : Categorie.values()) {
            produsePeCategorii.put(c, new ArrayList<>());
        }
    }

    public void adauga(Categorie categorie, Produs produs) {
        Objects.requireNonNull(categorie, "categorie");
        Objects.requireNonNull(produs, "produs");
        produsePeCategorii.get(categorie).add(produs);
    }

    public List<Produs> produseDin(Categorie categorie) {
        return List.copyOf(produsePeCategorii.getOrDefault(categorie, List.of()));
    }

    public List<Produs> toateProdusele() {
        return produsePeCategorii.values().stream().flatMap(List::stream).toList();
    }

    public List<Produs> vegetarieneInOrdineAlfabetica() {
        Predicate<Produs> vegetarian = Produs::isVegetarian;
        return toateProdusele().stream()
                .filter(vegetarian)
                .sorted(Comparator.comparing(Produs::getNume))
                .toList();
    }

    public OptionalDouble pretMediuLa(Categorie categorie) {
        return produseDin(categorie).stream().mapToDouble(Produs::getPret).average();
    }

    public boolean existaPestePret(double prag) {
        return toateProdusele().stream().anyMatch(p -> p.getPret() > prag);
    }

    public Optional<Produs> cautaDupaNume(String nume) {
        if (nume == null || nume.isBlank()) return Optional.empty();
        String needle = nume.trim().toLowerCase();
        return toateProdusele().stream()
                .filter(p -> p.getNume().toLowerCase().equals(needle))
                .findFirst();
    }
}
