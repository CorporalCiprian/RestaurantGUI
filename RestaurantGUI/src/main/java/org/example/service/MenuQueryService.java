package org.example.service;

import org.example.Produs;
import org.example.persistence.MancareEntity;
import org.example.persistence.ProdusEntity;
import org.example.persistence.ProdusMapper;
import org.example.persistence.ProdusRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read-only menu query operations used by UI controllers.
 * Keeps all DB access out of controllers (MVC separation).
 */
public class MenuQueryService {

    private final ProdusRepository produsRepository;

    public MenuQueryService() {
        this(new ProdusRepository());
    }

    public MenuQueryService(ProdusRepository produsRepository) {
        this.produsRepository = produsRepository;
    }

    /** Returns the full menu as domain objects (Produs hierarchy). */
    public List<Produs> getAllProducts() {
        return produsRepository.findAll().stream().map(ProdusMapper::toDomain).toList();
    }

    /**
     * Returns a set of product names (lowercase) that are marked as dessert in persistence.
     * Used by Guest filters so no DB calls happen during stream filtering.
     */
    public Set<String> getDessertFoodNamesLowercase() {
        return produsRepository.findAll().stream()
                .filter(MancareEntity.class::isInstance)
                .map(MancareEntity.class::cast)
                .filter(MancareEntity::isDesert)
                .map(ProdusEntity::getNume)
                .filter(n -> n != null && !n.isBlank())
                .map(n -> n.trim().toLowerCase())
                .collect(Collectors.toSet());
    }

    /** Convenience map: name(lowercase) -> isDessert. */
    public Map<String, Boolean> getDessertFlagByNameLowercase() {
        return produsRepository.findAll().stream()
                .filter(MancareEntity.class::isInstance)
                .map(MancareEntity.class::cast)
                .collect(Collectors.toMap(
                        p -> p.getNume() == null ? "" : p.getNume().trim().toLowerCase(),
                        MancareEntity::isDesert,
                        (a, b) -> a
                ));
    }
}
