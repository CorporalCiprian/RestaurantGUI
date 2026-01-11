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

public class MenuQueryService {

    private final ProdusRepository produsRepository;

    public MenuQueryService() {
        this(new ProdusRepository());
    }

    public MenuQueryService(ProdusRepository produsRepository) {
        this.produsRepository = produsRepository;
    }

    public List<Produs> getAllProducts() {
        return produsRepository.findAll().stream().map(ProdusMapper::toDomain).toList();
    }

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
