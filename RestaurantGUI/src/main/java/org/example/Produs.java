package org.example;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.text.DecimalFormat;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tip")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Mancare.class, name = "MANCARE"),
        @JsonSubTypes.Type(value = Bautura.class, name = "BAUTURA"),
        @JsonSubTypes.Type(value = Pizza.class, name = "PIZZA")
})
public sealed abstract class Produs permits Mancare, Bautura, Pizza {
    // Use JavaFX properties so the GUI can bind directly to model values
    private final StringProperty nume;
    // make pret mutable so GUI can update it in-memory
    private final DoubleProperty pret;
    private final BooleanProperty vegetarian;

    protected Produs(String nume, double pret, boolean vegetarian) {
        this.nume = new SimpleStringProperty(Objects.requireNonNull(nume, "nume"));
        if (pret < 0) throw new IllegalArgumentException("Pretul trebuie sa fie >= 0");
        this.pret = new SimpleDoubleProperty(pret);
        this.vegetarian = new SimpleBooleanProperty(vegetarian);
    }

    // Java-friendly getters (keep existing API)
    public String getNume() {
        return nume.get();
    }

    // Property accessor for bindings
    public StringProperty numeProperty() {
        return nume;
    }

    public double getPret() {
        return pret.get();
    }

    public DoubleProperty pretProperty() {
        return pret;
    }

    /**
     * Seteaza pretul (in-memory). Valideaza valoarea (> = 0).
     * Observatie: equals/hashCode nu includ pretul pentru a evita probleme
     * cand obiectele sunt folosite ca chei in harti si pretul se modifica.
     */
    public void setPret(double pret) {
        if (pret < 0) throw new IllegalArgumentException("Pretul trebuie sa fie >= 0");
        this.pret.set(pret);
    }

    public boolean isVegetarian() {
        return vegetarian.get();
    }

    public BooleanProperty vegetarianProperty() {
        return vegetarian;
    }

    protected String formatPret() {
        DecimalFormat df = new DecimalFormat("#0.0");
        return df.format(pret.get());
    }

    @Override
    public String toString() {
        return getNume() + " - " + formatPret() + " RON";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produs produs = (Produs) o;
        // Do NOT include pret in equality so that mutable price updates do not
        // break collections/maps that use Produs as key. Equality is defined
        // by runtime class and name only.
        return getNume().equals(produs.getNume());
    }

    @Override
    public int hashCode() {
        // See note in equals: omit pret from hashCode to keep identity stable
        return Objects.hash(getClass(), getNume());
    }
}
