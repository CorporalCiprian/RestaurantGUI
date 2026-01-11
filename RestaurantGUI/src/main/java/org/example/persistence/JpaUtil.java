package org.example.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {
    private static volatile EntityManagerFactory emf;

    private JpaUtil() {
    }

    public static EntityManagerFactory emf() {
        EntityManagerFactory local = emf;
        if (local == null) {
            synchronized (JpaUtil.class) {
                local = emf;
                if (local == null) {
                    emf = local = Persistence.createEntityManagerFactory("restaurantPU");
                }
            }
        }
        return local;
    }

    public static EntityManager em() {
        return emf().createEntityManager();
    }

    public static void shutdown() {
        EntityManagerFactory local = emf;
        if (local != null && local.isOpen()) {
            local.close();
        }
        emf = null;
    }
}
