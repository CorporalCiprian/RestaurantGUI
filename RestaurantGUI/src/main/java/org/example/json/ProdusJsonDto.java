package org.example.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * JSON DTO for {@code Produs} hierarchy.
 *
 * We keep a stable on-disk format independent of the JavaFX domain model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tip")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PizzaJsonDto.class, name = "PIZZA"),
        @JsonSubTypes.Type(value = MancareJsonDto.class, name = "MANCARE"),
        @JsonSubTypes.Type(value = BauturaJsonDto.class, name = "BAUTURA")
})
public sealed interface ProdusJsonDto permits MancareJsonDto, BauturaJsonDto, PizzaJsonDto {
    String getNume();

    void setNume(String nume);

    double getPret();

    void setPret(double pret);

    boolean isVegetarian();

    void setVegetarian(boolean vegetarian);
}
