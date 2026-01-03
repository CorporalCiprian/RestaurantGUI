package org.example.json;

import org.example.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps between the domain model ({@link Produs}) and JSON DTOs.
 *
 * Domain classes are not Jackson-friendly (no default constructors), so we
 * serialize/deserialize via DTOs.
 */
public final class ProdusJsonMapper {
    private ProdusJsonMapper() {
    }

    public static ProdusJsonDto toDto(Produs p) {
        if (p == null) return null;
        if (p instanceof Pizza pizza) {
            return new PizzaJsonDto(
                    pizza.getNume(),
                    pizza.getPret(),
                    pizza.isVegetarian(),
                    pizza.getBlat(),
                    pizza.getSos(),
                    new ArrayList<>(pizza.getToppings())
            );
        }
        if (p instanceof Mancare m) {
            return new MancareJsonDto(m.getNume(), m.getPret(), m.isVegetarian(), m.getGramaj());
        }
        if (p instanceof Bautura b) {
            return new BauturaJsonDto(b.getNume(), b.getPret(), b.isVegetarian(), b.getVolum());
        }
        throw new IllegalArgumentException("Tip produs necunoscut: " + p.getClass());
    }

    /**
     * Backward compatible: also accepts JSONs that don't contain "tip" by inferring type.
     */
    public static Produs toDomain(ProdusJsonDto dto) {
        if (dto == null) return null;

        if (dto instanceof PizzaJsonDto p) {
            Pizza.TipBlat blat = p.getBlat() == null ? Pizza.TipBlat.CLASIC : p.getBlat();
            Pizza.TipSos sos = p.getSos() == null ? Pizza.TipSos.ROSU : p.getSos();

            Pizza.Builder builder = new Pizza.Builder(blat, sos)
                    .nume(dto.getNume())
                    .vegetarian(dto.isVegetarian())
                    // baza is not stored separately; use total price and then override.
                    .baza(dto.getPret());

            if (p.getToppings() != null) {
                builder.adaugaToppingi(p.getToppings());
            }

            Pizza pizza = builder.build();
            // Keep the exact stored price (don't force recomputation)
            pizza.setPret(dto.getPret());
            return pizza;
        }

        if (dto instanceof MancareJsonDto m) {
            int gramaj = m.getGramaj() <= 0 ? 1 : m.getGramaj();
            return new Mancare(dto.getNume(), dto.getPret(), gramaj, dto.isVegetarian());
        }

        if (dto instanceof BauturaJsonDto b) {
            int volum = b.getVolum() <= 0 ? 1 : b.getVolum();
            return new Bautura(dto.getNume(), dto.getPret(), volum, dto.isVegetarian());
        }

        throw new IllegalArgumentException("Tip DTO necunoscut: " + dto.getClass());
    }

    /**
     * Inference helper for legacy files (without "tip").
     */
    public static ProdusJsonDto inferDto(String nume, double pret, boolean vegetarian,
                                        Integer gramaj, Integer volum,
                                        Pizza.TipBlat blat, Pizza.TipSos sos,
                                        List<Pizza.Topping> toppings) {
        if (blat != null || sos != null || (toppings != null)) {
            return new PizzaJsonDto(nume, pret, vegetarian, blat, sos, topsOrEmpty(toppings));
        }
        if (gramaj != null) {
            return new MancareJsonDto(nume, pret, vegetarian, gramaj);
        }
        if (volum != null) {
            return new BauturaJsonDto(nume, pret, vegetarian, volum);
        }
        // Default fallback
        return new MancareJsonDto(nume, pret, vegetarian, 1);
    }

    private static List<Pizza.Topping> topsOrEmpty(List<Pizza.Topping> toppings) {
        return toppings == null ? List.of() : toppings;
    }
}

