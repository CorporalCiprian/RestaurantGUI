package org.example.json;

public final class MancareJsonDto implements ProdusJsonDto {
    private String nume;
    private double pret;
    private boolean vegetarian;
    private int gramaj;

    public MancareJsonDto() {
    }

    public MancareJsonDto(String nume, double pret, boolean vegetarian, int gramaj) {
        this.nume = nume;
        this.pret = pret;
        this.vegetarian = vegetarian;
        this.gramaj = gramaj;
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

    public int getGramaj() {
        return gramaj;
    }

    public void setGramaj(int gramaj) {
        this.gramaj = gramaj;
    }
}
