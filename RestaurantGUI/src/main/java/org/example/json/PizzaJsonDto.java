package org.example.json;

import org.example.Pizza;

import java.util.ArrayList;
import java.util.List;

public final class PizzaJsonDto implements ProdusJsonDto {
    private String nume;
    private double pret;
    private boolean vegetarian;
    private Pizza.TipBlat blat;
    private Pizza.TipSos sos;
    private List<Pizza.Topping> toppings = new ArrayList<>();

    public PizzaJsonDto() {
    }

    public PizzaJsonDto(String nume, double pret, boolean vegetarian, Pizza.TipBlat blat, Pizza.TipSos sos, List<Pizza.Topping> toppings) {
        this.nume = nume;
        this.pret = pret;
        this.vegetarian = vegetarian;
        this.blat = blat;
        this.sos = sos;
        this.toppings = toppings == null ? new ArrayList<>() : new ArrayList<>(toppings);
    }

    @Override
    public String getNume() {
        return nume;
    }

    @Override
    public void setNume(String nume) {
        this.nume = nume;
    }

    @Override
    public double getPret() {
        return pret;
    }

    @Override
    public void setPret(double pret) {
        this.pret = pret;
    }

    @Override
    public boolean isVegetarian() {
        return vegetarian;
    }

    @Override
    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Pizza.TipBlat getBlat() {
        return blat;
    }

    public void setBlat(Pizza.TipBlat blat) {
        this.blat = blat;
    }

    public Pizza.TipSos getSos() {
        return sos;
    }

    public void setSos(Pizza.TipSos sos) {
        this.sos = sos;
    }

    public List<Pizza.Topping> getToppings() {
        return toppings;
    }

    public void setToppings(List<Pizza.Topping> toppings) {
        this.toppings = toppings == null ? new ArrayList<>() : new ArrayList<>(toppings);
    }
}

