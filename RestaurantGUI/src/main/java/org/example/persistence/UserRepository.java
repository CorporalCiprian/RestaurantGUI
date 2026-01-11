package org.example.persistence;

import org.example.model.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository() {
        this.entityManager = JpaUtil.em();
    }

    public Optional<User> findById(long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    public Optional<User> findByUsername(String username) {
        try {
            return Optional.of(entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void save(User user) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();
    }

    public User saveOrUpdate(User user) {
        entityManager.getTransaction().begin();
        User managed = entityManager.merge(user);
        entityManager.getTransaction().commit();
        return managed;
    }

    public java.util.List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public void delete(User user) {
        entityManager.getTransaction().begin();
        if (!entityManager.contains(user)) {
            user = entityManager.merge(user);
        }
        entityManager.remove(user);
        entityManager.getTransaction().commit();
    }
}
