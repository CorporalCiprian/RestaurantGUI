package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mese")
public class Masa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numar;

    public Masa() {
    }

    public Masa(int numar) {
        this.numar = numar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumar() {
        return numar;
    }

    public void setNumar(int numar) {
        this.numar = numar;
    }

    @Override
    public String toString() {
        return "Table " + numar;
    }
}

