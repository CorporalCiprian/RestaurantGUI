package org.example.persistence;

import org.example.model.Comanda;
import org.example.model.User;

import jakarta.persistence.EntityManager;
import java.util.List;

public class ComandaRepository {
    private final EntityManager entityManager;

    public ComandaRepository() {
        this.entityManager = JpaUtil.em();
    }

    public void save(Comanda comanda) {
        entityManager.getTransaction().begin();
        entityManager.persist(comanda);
        entityManager.getTransaction().commit();
    }

    public void delete(Comanda comanda) {
        entityManager.getTransaction().begin();
        if (!entityManager.contains(comanda)) {
            comanda = entityManager.merge(comanda);
        }
        entityManager.remove(comanda);
        entityManager.getTransaction().commit();
    }


    public List<Comanda> findByOspatar(User ospatar) {
        return entityManager.createQuery("SELECT c FROM Comanda c WHERE c.ospatar = :ospatar", Comanda.class)
                .setParameter("ospatar", ospatar)
                .getResultList();
    }

    public List<Comanda> findAll() {
        return entityManager.createQuery("SELECT c FROM Comanda c", Comanda.class).getResultList();
    }
}

