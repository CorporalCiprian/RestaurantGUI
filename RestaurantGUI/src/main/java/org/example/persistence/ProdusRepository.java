package org.example.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/** Repository that manages transactions explicitly (RESOURCE_LOCAL). */
public class ProdusRepository {

    public long count() {
        try (EntityManager em = JpaUtil.em()) {
            return em.createQuery("select count(p) from ProdusEntity p", Long.class).getSingleResult();
        }
    }

    public List<ProdusEntity> findAll() {
        try (EntityManager em = JpaUtil.em()) {
            TypedQuery<ProdusEntity> q = em.createQuery("select p from ProdusEntity p order by p.nume", ProdusEntity.class);
            return q.getResultList();
        }
    }

    public Optional<ProdusEntity> findById(long id) {
        try (EntityManager em = JpaUtil.em()) {
            return Optional.ofNullable(em.find(ProdusEntity.class, id));
        }
    }

    public Optional<ProdusEntity> findByTypeAndName(String discriminatorTip, String nume) {
        if (nume == null || nume.isBlank() || discriminatorTip == null || discriminatorTip.isBlank()) return Optional.empty();
        try (EntityManager em = JpaUtil.em()) {
            return em.createQuery(
                            "select p from ProdusEntity p where type(p) = :clazz and lower(p.nume) = :nume",
                            ProdusEntity.class)
                    .setParameter("clazz", classForDiscriminator(discriminatorTip))
                    .setParameter("nume", nume.trim().toLowerCase())
                    .setMaxResults(1)
                    .getResultList()
                    .stream()
                    .findFirst();
        }
    }

    public ProdusEntity save(ProdusEntity entity) {
        if (entity == null) throw new IllegalArgumentException("entity");
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            ProdusEntity managed = em.merge(entity);
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void deleteById(long id) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            ProdusEntity e = em.find(ProdusEntity.class, id);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Upsert all provided entities (merge). This matches the "Import: add products in DB" requirement.
     */
    public void saveAll(List<ProdusEntity> all) {
        if (all == null || all.isEmpty()) return;
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            for (ProdusEntity e : all) {
                em.merge(e);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /** Replace all products with the provided list. */
    public void replaceAll(List<ProdusEntity> all) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            // Important: PizzaEntity has an @ElementCollection stored in a separate table (pizza_toppings)
            // with a FK to produse(id). We must delete child rows first to avoid FK violations.
            em.createNativeQuery("delete from pizza_toppings").executeUpdate();

            em.createQuery("delete from ProdusEntity").executeUpdate();
            if (all != null) {
                for (ProdusEntity e : all) {
                    em.persist(e);
                }
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    private static Class<? extends ProdusEntity> classForDiscriminator(String tip) {
        return switch (tip.toUpperCase()) {
            case "PIZZA" -> PizzaEntity.class;
            case "MANCARE" -> MancareEntity.class;
            case "BAUTURA" -> BauturaEntity.class;
            default -> ProdusEntity.class;
        };
    }
}
