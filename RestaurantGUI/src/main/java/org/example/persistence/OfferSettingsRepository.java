package org.example.persistence;

import jakarta.persistence.EntityManager;
import org.example.model.OfferSettings;

import java.util.Optional;

public class OfferSettingsRepository {

    public Optional<OfferSettings> get() {
        try (EntityManager em = JpaUtil.em()) {
            return Optional.ofNullable(em.find(OfferSettings.class, 1));
        }
    }

    public OfferSettings save(OfferSettings settings) {
        if (settings == null) throw new IllegalArgumentException("settings");
        settings.setId(1);
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            OfferSettings managed = em.merge(settings);
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public OfferSettings ensureDefault() {
        return get().orElseGet(() -> save(new OfferSettings(true, true, true)));
    }
}
