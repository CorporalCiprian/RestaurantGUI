package org.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BAUTURA")
public class BauturaEntity extends ProdusEntity {

    @Column
    private Integer volum;

    public BauturaEntity() {
    }

    public BauturaEntity(String nume, double pret, boolean vegetarian, int volum) {
        super(nume, pret, vegetarian);
        this.volum = volum;
    }

    public Integer getVolum() {
        return volum;
    }

    public void setVolum(Integer volum) {
        this.volum = volum;
    }
}

