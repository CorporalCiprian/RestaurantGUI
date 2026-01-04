package org.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANCARE")
public class MancareEntity extends ProdusEntity {

    @Column
    private Integer gramaj;

    @Column(name = "is_desert")
    private Boolean desert;

    public MancareEntity() {
    }

    public MancareEntity(String nume, double pret, boolean vegetarian, int gramaj) {
        this(nume, pret, vegetarian, gramaj, false);
    }

    public MancareEntity(String nume, double pret, boolean vegetarian, int gramaj, boolean desert) {
        super(nume, pret, vegetarian);
        this.gramaj = gramaj;
        this.desert = desert;
    }

    public Integer getGramaj() {
        return gramaj;
    }

    public void setGramaj(Integer gramaj) {
        this.gramaj = gramaj;
    }

    public boolean isDesert() {
        return Boolean.TRUE.equals(desert);
    }

    public void setDesert(boolean desert) {
        this.desert = desert;
    }
}
