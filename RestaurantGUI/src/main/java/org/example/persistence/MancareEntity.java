package org.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANCARE")
public class MancareEntity extends ProdusEntity {

    @Column
    private Integer gramaj;

    public MancareEntity() {
    }

    public MancareEntity(String nume, double pret, boolean vegetarian, int gramaj) {
        super(nume, pret, vegetarian);
        this.gramaj = gramaj;
    }

    public Integer getGramaj() {
        return gramaj;
    }

    public void setGramaj(Integer gramaj) {
        this.gramaj = gramaj;
    }
}

