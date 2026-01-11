package org.example.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "produse")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tip", discriminatorType = DiscriminatorType.STRING)
public abstract class ProdusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nume;

    @Column(nullable = false)
    private double pret;

    @Column(nullable = false)
    private boolean vegetarian;

    protected ProdusEntity() {
    }

    protected ProdusEntity(String nume, double pret, boolean vegetarian) {
        this.nume = nume;
        this.pret = pret;
        this.vegetarian = vegetarian;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public double getPret() {
        return pret;
    }

    public void setPret(double pret) {
        this.pret = pret;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }
}
