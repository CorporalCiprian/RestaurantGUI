package org.example.persistence;

import org.example.*;

import java.util.LinkedHashSet;

/** Maps between JavaFX domain model (Produs) and JPA entities. */
public final class ProdusMapper {
    private ProdusMapper() {
    }

    public static ProdusEntity toEntity(Produs produs) {
        if (produs == null) return null;

        if (produs instanceof Pizza p) {
            return new PizzaEntity(
                    p.getNume(),
                    p.getPret(),
                    p.isVegetarian(),
                    p.getBlat(),
                    p.getSos(),
                    new LinkedHashSet<>(p.getToppings())
            );
        }
        if (produs instanceof Mancare m) {
            // Domain model doesn't carry dessert flag; default = false.
            return new MancareEntity(m.getNume(), m.getPret(), m.isVegetarian(), m.getGramaj(), false);
        }
        if (produs instanceof Bautura b) {
            return new BauturaEntity(b.getNume(), b.getPret(), b.isVegetarian(), b.getVolum());
        }

        throw new IllegalArgumentException("Tip produs necunoscut: " + produs.getClass());
    }

    /**
     * Maps a domain object to an entity, reusing the ID from an existing DB entity if provided.
     * This enables save via merge() to perform UPDATE and keep stable IDs.
     */
    public static ProdusEntity toEntityWithIdIfPresent(Produs produs, ProdusEntity existing) {
        ProdusEntity created = toEntity(produs);
        if (created != null && existing != null && existing.getId() != null) {
            created.setId(existing.getId());
        }
        return created;
    }

    public static Produs toDomain(ProdusEntity e) {
        if (e == null) return null;

        if (e instanceof PizzaEntity p) {
            Pizza.Builder builder = new Pizza.Builder(
                    p.getBlat() == null ? Pizza.TipBlat.CLASIC : p.getBlat(),
                    p.getSos() == null ? Pizza.TipSos.ROSU : p.getSos()
            )
                    .nume(e.getNume())
                    .vegetarian(e.isVegetarian())
                    .baza(e.getPret());

            // Note: baza in Builder recomputes price with toppings; here we need exact stored price.
            // We'll build with toppings, then override the price to the stored value.
            if (p.getToppings() != null) {
                for (Pizza.Topping t : p.getToppings()) builder.adaugaTopping(t);
            }
            Pizza pizza = builder.build();
            pizza.setPret(e.getPret());
            return pizza;
        }
        if (e instanceof MancareEntity m) {
            int gramaj = m.getGramaj() == null ? 1 : m.getGramaj();
            // Domain model doesn't have dessert; keep vegetarian as-is.
            return new Mancare(e.getNume(), e.getPret(), gramaj, e.isVegetarian());
        }
        if (e instanceof BauturaEntity b) {
            int volum = b.getVolum() == null ? 1 : b.getVolum();
            return new Bautura(e.getNume(), e.getPret(), volum, e.isVegetarian());
        }

        // Fallback: treat as generic food
        return new Mancare(e.getNume(), e.getPret(), 1, e.isVegetarian());
    }
}
