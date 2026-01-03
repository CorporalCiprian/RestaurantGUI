package org.example.persistence;

import jakarta.persistence.*;
import org.example.Pizza;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("PIZZA")
public class PizzaEntity extends ProdusEntity {

    @Enumerated(EnumType.STRING)
    @Column
    private Pizza.TipBlat blat;

    @Enumerated(EnumType.STRING)
    @Column
    private Pizza.TipSos sos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pizza_toppings", joinColumns = @JoinColumn(name = "pizza_id"))
    @Column(name = "topping")
    @Enumerated(EnumType.STRING)
    private Set<Pizza.Topping> toppings = new LinkedHashSet<>();

    public PizzaEntity() {
    }

    public PizzaEntity(String nume, double pret, boolean vegetarian, Pizza.TipBlat blat, Pizza.TipSos sos, Set<Pizza.Topping> toppings) {
        super(nume, pret, vegetarian);
        this.blat = blat;
        this.sos = sos;
        if (toppings != null) this.toppings = new LinkedHashSet<>(toppings);
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

    public Set<Pizza.Topping> getToppings() {
        return toppings;
    }

    public void setToppings(Set<Pizza.Topping> toppings) {
        this.toppings = (toppings == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(toppings);
    }
}

