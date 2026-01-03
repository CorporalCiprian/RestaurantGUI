package org.example.json;

public final class BauturaJsonDto implements ProdusJsonDto {
    private String nume;
    private double pret;
    private boolean vegetarian;
    private int volum;

    public BauturaJsonDto() {
    }

    public BauturaJsonDto(String nume, double pret, boolean vegetarian, int volum) {
        this.nume = nume;
        this.pret = pret;
        this.vegetarian = vegetarian;
        this.volum = volum;
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

    public int getVolum() {
        return volum;
    }

    public void setVolum(int volum) {
        this.volum = volum;
    }
}

