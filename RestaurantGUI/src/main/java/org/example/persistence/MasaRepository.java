package org.example.persistence;

import org.example.model.Masa;

import jakarta.persistence.EntityManager;
import java.util.List;

public class MasaRepository {
    private final EntityManager entityManager;

    public MasaRepository() {
        this.entityManager = JpaUtil.em();
    }

    public List<Masa> findAll() {
        return entityManager.createQuery("SELECT m FROM Masa m", Masa.class).getResultList();
    }

    public void save(Masa masa) {
        entityManager.getTransaction().begin();
        entityManager.persist(masa);
        entityManager.getTransaction().commit();
    }
}
